package com.sahrin.spas.service;

import com.sahrin.spas.dto.performance.AdminMarkOverrideRequest;
import com.sahrin.spas.dto.performance.AssessmentRequest;
import com.sahrin.spas.dto.performance.AssessmentResponse;
import com.sahrin.spas.dto.performance.MarkEntryRequest;
import com.sahrin.spas.dto.performance.PublishResultsRequest;
import com.sahrin.spas.dto.performance.StudentMarkResponse;
import com.sahrin.spas.dto.performance.StudentPublishedResultsResponse;
import com.sahrin.spas.dto.performance.SubjectResultPublicationResponse;
import com.sahrin.spas.dto.performance.UnlockRequest;
import com.sahrin.spas.entity.Assessment;
import com.sahrin.spas.entity.AssessmentType;
import com.sahrin.spas.entity.Course;
import com.sahrin.spas.entity.Department;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.MarkAuditLog;
import com.sahrin.spas.entity.ResultPublicationStatus;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.StudentMark;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.SubjectResultPublication;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.InvalidStatusTransitionException;
import com.sahrin.spas.repository.AssessmentRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.MarkAuditLogRepository;
import com.sahrin.spas.repository.StudentMarkRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.SubjectResultPublicationRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.PerformanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private StudentMarkRepository studentMarkRepository;

    @Mock
    private SubjectResultPublicationRepository publicationRepository;

    @Mock
    private MarkAuditLogRepository auditLogRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LecturerAssignmentRepository lecturerAssignmentRepository;

    @Spy
    private GradingPolicy gradingPolicy = new GradingPolicy();

    @InjectMocks
    private PerformanceServiceImpl performanceService;

    private User lecturer;
    private User student;
    private User admin;
    private Department department;
    private Course course;
    private Subject subject;

    @BeforeEach
    void setUp() {
        lecturer = new User("Dr. Smith", "LEC-001", "smith@spas.edu", "hash", Role.LECTURER, UserStatus.ACTIVE);
        lecturer.setId(10L);

        student = new User("John Doe", "STU-001", "john@spas.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student.setId(20L);

        admin = new User("Admin User", "ADM-001", "admin@spas.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        department = new Department("CS", "Computer Science", "Desc");
        department.setId(1L);

        course = new Course("BCS", "Bachelor of Computer Science", "Desc", department);
        course.setId(10L);

        subject = new Subject("CS101", "Intro to Programming", "Desc", 3, 1, course, department);
        subject.setId(100L);
    }

    @Test
    @DisplayName("Lecturer creates assessment successfully when weight sum <= 100%")
    void testCreateAssessmentSuccess() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.empty());
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        Assessment saved = new Assessment("Midterm", AssessmentType.MIDTERM, 100.0, 30.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        saved.setId(1L);
        when(assessmentRepository.save(any(Assessment.class))).thenReturn(saved);

        AssessmentRequest request = new AssessmentRequest(
                "Midterm", AssessmentType.MIDTERM, 100.0, 30.0, 100L, "2025/2026", "SEMESTER_1");

        AssessmentResponse response = performanceService.createAssessment(request, "smith@spas.edu");

        assertNotNull(response);
        assertEquals("Midterm", response.getTitle());
        assertEquals(30.0, response.getWeightPercentage());
        verify(assessmentRepository).save(any(Assessment.class));
    }

    @Test
    @DisplayName("Create assessment fails if total weight would exceed 100%")
    void testCreateAssessmentExceedsWeightLimit() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.empty());

        Assessment existing = new Assessment("Midterm", AssessmentType.MIDTERM, 100.0, 60.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(existing));

        AssessmentRequest request = new AssessmentRequest(
                "Final Exam", AssessmentType.FINAL_EXAM, 100.0, 50.0, 100L, "2025/2026", "SEMESTER_1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> performanceService.createAssessment(request, "smith@spas.edu"));
        assertTrue(ex.getMessage().contains("Total assessment weight cannot exceed 100"));
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lecturer not assigned to subject cannot create assessment")
    void testCreateAssessmentUnauthorizedLecturer() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(false);

        AssessmentRequest request = new AssessmentRequest(
                "Midterm", AssessmentType.MIDTERM, 100.0, 30.0, 100L, "2025/2026", "SEMESTER_1");

        assertThrows(AccessDeniedException.class, () -> performanceService.createAssessment(request, "smith@spas.edu"));
    }

    @Test
    @DisplayName("Enter mark rejects score greater than maxMarks")
    void testEnterMarkInvalidScoreAboveMax() {
        Assessment assessment = new Assessment("Quiz 1", AssessmentType.QUIZ, 50.0, 20.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        assessment.setId(5L);

        Enrollment enrollment = new Enrollment(student, course, subject, "2025/2026", "SEMESTER_1");
        enrollment.setId(200L);

        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(assessmentRepository.findById(5L)).thenReturn(Optional.of(assessment));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(enrollment));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.empty());

        MarkEntryRequest request = new MarkEntryRequest(5L, 200L, 55.0, "Too high");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> performanceService.enterOrUpdateMark(request, "smith@spas.edu"));
        assertTrue(ex.getMessage().contains("maximum mark"));
    }

    @Test
    @DisplayName("Enter mark rejects score less than 0")
    void testEnterMarkNegativeScore() {
        Assessment assessment = new Assessment("Quiz 1", AssessmentType.QUIZ, 50.0, 20.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        assessment.setId(5L);

        Enrollment enrollment = new Enrollment(student, course, subject, "2025/2026", "SEMESTER_1");
        enrollment.setId(200L);

        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(assessmentRepository.findById(5L)).thenReturn(Optional.of(assessment));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(enrollment));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.empty());

        MarkEntryRequest request = new MarkEntryRequest(5L, 200L, -5.0, "Negative");

        assertThrows(IllegalArgumentException.class, () -> performanceService.enterOrUpdateMark(request, "smith@spas.edu"));
    }

    @Test
    @DisplayName("Enter mark rejected if subject results are already PUBLISHED")
    void testEnterMarkWhenPublished() {
        Assessment assessment = new Assessment("Quiz 1", AssessmentType.QUIZ, 50.0, 20.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        assessment.setId(5L);

        SubjectResultPublication pub = new SubjectResultPublication(subject, "2025/2026", "SEMESTER_1",
                ResultPublicationStatus.PUBLISHED, lecturer, LocalDateTime.now());

        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(assessmentRepository.findById(5L)).thenReturn(Optional.of(assessment));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.of(pub));

        MarkEntryRequest request = new MarkEntryRequest(5L, 200L, 40.0, "Good");

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> performanceService.enterOrUpdateMark(request, "smith@spas.edu"));
        assertTrue(ex.getMessage().contains("published"));
    }

    @Test
    @DisplayName("Publishing fails if total assessment weight is not exactly 100%")
    void testPublishFailsWhenWeightNot100() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);

        Assessment a1 = new Assessment("A1", AssessmentType.MIDTERM, 100.0, 40.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(a1));

        PublishResultsRequest request = new PublishResultsRequest(100L, "2025/2026", "SEMESTER_1");

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> performanceService.publishSubjectResults(request, "smith@spas.edu"));
        assertTrue(ex.getMessage().contains("must equal exactly 100"));
    }

    @Test
    @DisplayName("Publishing succeeds when total assessment weight is exactly 100%")
    void testPublishSuccess() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);

        Assessment a1 = new Assessment("Midterm", AssessmentType.MIDTERM, 100.0, 40.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        Assessment a2 = new Assessment("Final", AssessmentType.FINAL_EXAM, 100.0, 60.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(a1, a2));
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.empty());

        when(publicationRepository.save(any(SubjectResultPublication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PublishResultsRequest request = new PublishResultsRequest(100L, "2025/2026", "SEMESTER_1");
        SubjectResultPublicationResponse response = performanceService.publishSubjectResults(request, "smith@spas.edu");

        assertNotNull(response);
        assertEquals(ResultPublicationStatus.PUBLISHED, response.getStatus());
        assertNotNull(response.getPublishedAt());
    }

    @Test
    @DisplayName("Admin unlock transitions status back to DRAFT and creates audit log")
    void testAdminUnlockResults() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));

        SubjectResultPublication pub = new SubjectResultPublication(subject, "2025/2026", "SEMESTER_1",
                ResultPublicationStatus.PUBLISHED, lecturer, LocalDateTime.now());
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.of(pub));
        when(publicationRepository.save(any(SubjectResultPublication.class))).thenReturn(pub);

        UnlockRequest req = new UnlockRequest("Correction needed for quiz 1");
        SubjectResultPublicationResponse resp = performanceService.unlockSubjectResults(100L, "2025/2026", "SEMESTER_1", req, "admin@spas.edu");

        assertEquals(ResultPublicationStatus.DRAFT, resp.getStatus());
        verify(auditLogRepository).save(any(MarkAuditLog.class));
    }

    @Test
    @DisplayName("Admin mark override updates mark and records audit log")
    void testAdminMarkOverride() {
        Assessment assessment = new Assessment("Final Exam", AssessmentType.FINAL_EXAM, 100.0, 50.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        Enrollment enrollment = new Enrollment(student, course, subject, "2025/2026", "SEMESTER_1");
        enrollment.setId(200L);

        StudentMark mark = new StudentMark(assessment, enrollment, 70.0, "Good", lecturer);
        mark.setId(50L);

        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(studentMarkRepository.findById(50L)).thenReturn(Optional.of(mark));
        when(studentMarkRepository.save(any(StudentMark.class))).thenAnswer(i -> i.getArgument(0));

        AdminMarkOverrideRequest request = new AdminMarkOverrideRequest(85.0, "Score recounting confirmed 85");
        StudentMarkResponse resp = performanceService.adminOverrideMark(50L, request, "admin@spas.edu");

        assertEquals(85.0, mark.getScore());
        verify(auditLogRepository).save(any(MarkAuditLog.class));
    }

    @Test
    @DisplayName("Student view hides results when subject is still DRAFT (unpublished)")
    void testStudentViewHidesDraftResults() {
        Enrollment enrollment = new Enrollment(student, course, subject, "2025/2026", "SEMESTER_1");
        enrollment.setId(200L);

        when(userRepository.findByEmail("john@spas.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(enrollment));
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.empty());

        StudentPublishedResultsResponse resp = performanceService.getMySubjectResults(200L, "john@spas.edu");

        assertNotNull(resp);
        assertEquals(ResultPublicationStatus.DRAFT, resp.getPublicationStatus());
        assertTrue(resp.getAssessmentResults().isEmpty());
        assertNull(resp.getTotalWeightedScore());
        assertNull(resp.getGrade());
    }

    @Test
    @DisplayName("Student view calculates published results with correct Grade and provisional notice")
    void testStudentViewPublishedResults() {
        SubjectResultPublication pub = new SubjectResultPublication(subject, "2025/2026", "SEMESTER_1",
                ResultPublicationStatus.PUBLISHED, lecturer, LocalDateTime.now());

        Enrollment enrollment = new Enrollment(student, course, subject, "2025/2026", "SEMESTER_1");
        enrollment.setId(200L);

        Assessment a1 = new Assessment("Midterm", AssessmentType.MIDTERM, 100.0, 40.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        a1.setId(1L);
        Assessment a2 = new Assessment("Final", AssessmentType.FINAL_EXAM, 100.0, 60.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        a2.setId(2L);

        StudentMark m1 = new StudentMark(a1, enrollment, 80.0, "Well done", lecturer); // 80/100 * 40 = 32.0
        StudentMark m2 = new StudentMark(a2, enrollment, 70.0, "Good", lecturer);      // 70/100 * 60 = 42.0
        // Total = 74.0 -> Grade B

        when(userRepository.findByEmail("john@spas.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(enrollment));
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.of(pub));
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(a1, a2));
        when(studentMarkRepository.findByEnrollmentId(200L)).thenReturn(List.of(m1, m2));

        StudentPublishedResultsResponse resp = performanceService.getMySubjectResults(200L, "john@spas.edu");

        assertNotNull(resp);
        assertEquals(ResultPublicationStatus.PUBLISHED, resp.getPublicationStatus());
        assertEquals(2, resp.getAssessmentResults().size());
        assertEquals(74.0, resp.getTotalWeightedScore());
        assertEquals(Grade.B, resp.getGrade());
        assertNotNull(resp.getGradingNotice());
    }

    @Test
    @DisplayName("DROPPED student gets Grade W even when results are published")
    void testStudentViewDroppedGetsGradeW() {
        SubjectResultPublication pub = new SubjectResultPublication(subject, "2025/2026", "SEMESTER_1",
                ResultPublicationStatus.PUBLISHED, lecturer, LocalDateTime.now());

        Enrollment enrollment = new Enrollment(student, course, subject, "2025/2026", "SEMESTER_1");
        enrollment.setId(200L);
        enrollment.setStatus(EnrollmentStatus.DROPPED);

        Assessment a1 = new Assessment("Final", AssessmentType.FINAL_EXAM, 100.0, 100.0,
                subject, "2025/2026", "SEMESTER_1", lecturer);
        a1.setId(1L);
        StudentMark m1 = new StudentMark(a1, enrollment, 90.0, "Great", lecturer);

        when(userRepository.findByEmail("john@spas.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(enrollment));
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(Optional.of(pub));
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(a1));
        when(studentMarkRepository.findByEnrollmentId(200L)).thenReturn(List.of(m1));

        StudentPublishedResultsResponse resp = performanceService.getMySubjectResults(200L, "john@spas.edu");

        assertEquals(Grade.W, resp.getGrade());
    }
}
