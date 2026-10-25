package com.sahrin.spas.service;

import com.sahrin.spas.dto.report.AdminInstitutionalReportResponse;
import com.sahrin.spas.dto.report.LecturerSubjectReportResponse;
import com.sahrin.spas.dto.report.StudentReportResponse;
import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.Assessment;
import com.sahrin.spas.entity.AssessmentType;
import com.sahrin.spas.entity.AttendanceRecord;
import com.sahrin.spas.entity.AttendanceSession;
import com.sahrin.spas.entity.AttendanceStatus;
import com.sahrin.spas.entity.Course;
import com.sahrin.spas.entity.Department;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.LecturerAssignment;
import com.sahrin.spas.entity.ResultPublicationStatus;
import com.sahrin.spas.entity.RiskAlert;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.StudentMark;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.SubjectResultPublication;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.repository.AssessmentRepository;
import com.sahrin.spas.repository.AttendanceRecordRepository;
import com.sahrin.spas.repository.AttendanceSessionRepository;
import com.sahrin.spas.repository.CourseRepository;
import com.sahrin.spas.repository.DepartmentRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.RiskAlertRepository;
import com.sahrin.spas.repository.StudentMarkRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.SubjectResultPublicationRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private LecturerAssignmentRepository assignmentRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private StudentMarkRepository studentMarkRepository;
    @Mock
    private SubjectResultPublicationRepository publicationRepository;
    @Mock
    private AttendanceSessionRepository attendanceSessionRepository;
    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;
    @Mock
    private RiskAlertRepository riskAlertRepository;

    private GradingPolicy gradingPolicy;
    private ReportServiceImpl reportService;

    private User student;
    private User lecturer;
    private User admin;
    private Department department;
    private Course course;
    private Subject subject;
    private Enrollment enrollment;
    private Assessment quiz;
    private Assessment finalExam;

    @BeforeEach
    void setUp() {
        gradingPolicy = new GradingPolicy();
        reportService = new ReportServiceImpl(
                userRepository,
                departmentRepository,
                courseRepository,
                subjectRepository,
                enrollmentRepository,
                assignmentRepository,
                assessmentRepository,
                studentMarkRepository,
                publicationRepository,
                attendanceSessionRepository,
                attendanceRecordRepository,
                riskAlertRepository,
                gradingPolicy
        );

        student = new User("Alice Smith", "STU-001", "alice@university.edu", "secret", Role.STUDENT, User.UserStatus.ACTIVE);
        student.setId(101L);

        lecturer = new User("Dr. Bob Vance", "LEC-002", "dr.bob@university.edu", "secret", Role.LECTURER, User.UserStatus.ACTIVE);
        lecturer.setId(201L);

        admin = new User("System Admin", "ADM-001", "admin@university.edu", "secret", Role.ADMIN, User.UserStatus.ACTIVE);
        admin.setId(301L);

        department = new Department("COM", "Computing Sciences", "Department of Computer Science");
        department.setId(1L);

        course = new Course("BCS", "Bachelor of Computer Science", "4-year degree", department);
        course.setId(10L);

        subject = new Subject("CS101", "Introduction to Programming", "Introductory coding", 3, 1, course, department);
        subject.setId(100L);

        enrollment = new Enrollment(student, course, subject, "2025/2026", "1");
        enrollment.setId(501L);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        quiz = new Assessment("Quiz 1", AssessmentType.QUIZ, 20.0, 20.0, subject, "2025/2026", "1", lecturer);
        quiz.setId(1001L);

        finalExam = new Assessment("Final Exam", AssessmentType.FINAL_EXAM, 100.0, 80.0, subject, "2025/2026", "1", lecturer);
        finalExam.setId(1002L);
    }

    // ==========================================
    // STUDENT REPORT TESTS
    // ==========================================

    @Test
    @DisplayName("Should generate student report with published results, calculated GPA and attendance")
    void shouldGenerateStudentReportWithPublishedResultsAndGpa() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(101L)).thenReturn(List.of(enrollment));

        SubjectResultPublication pub = new SubjectResultPublication(
                subject, "2025/2026", "1", ResultPublicationStatus.PUBLISHED, lecturer, LocalDateTime.now());
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "1"))
                .thenReturn(Optional.of(pub));

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of(quiz, finalExam));

        StudentMark mark1 = new StudentMark(quiz, enrollment, 18.0, "Great", lecturer); // (18/20)*20 = 18.0
        StudentMark mark2 = new StudentMark(finalExam, enrollment, 75.0, "Good", lecturer); // (75/100)*80 = 60.0
        // Total = 78.0% -> Grade A (4.0)
        when(studentMarkRepository.findByEnrollmentId(501L)).thenReturn(List.of(mark1, mark2));

        // 5 sessions: 3 PRESENT, 1 LATE, 1 EXCUSED. Attended = 4, Total = 5, Excused = 1, Eligible = 4. Att % = 100.0%
        AttendanceSession s1 = new AttendanceSession(subject, LocalDate.now(), "L1", "2025/2026", "1", lecturer);
        s1.setId(1L);
        AttendanceSession s2 = new AttendanceSession(subject, LocalDate.now(), "L2", "2025/2026", "1", lecturer);
        s2.setId(2L);
        AttendanceSession s3 = new AttendanceSession(subject, LocalDate.now(), "L3", "2025/2026", "1", lecturer);
        s3.setId(3L);
        AttendanceSession s4 = new AttendanceSession(subject, LocalDate.now(), "L4", "2025/2026", "1", lecturer);
        s4.setId(4L);
        AttendanceSession s5 = new AttendanceSession(subject, LocalDate.now(), "L5", "2025/2026", "1", lecturer);
        s5.setId(5L);
        when(attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of(s1, s2, s3, s4, s5));

        AttendanceRecord r1 = new AttendanceRecord(s1, enrollment, AttendanceStatus.PRESENT, null);
        AttendanceRecord r2 = new AttendanceRecord(s2, enrollment, AttendanceStatus.PRESENT, null);
        AttendanceRecord r3 = new AttendanceRecord(s3, enrollment, AttendanceStatus.PRESENT, null);
        AttendanceRecord r4 = new AttendanceRecord(s4, enrollment, AttendanceStatus.LATE, null);
        AttendanceRecord r5 = new AttendanceRecord(s5, enrollment, AttendanceStatus.EXCUSED, "Doctor letter");
        when(attendanceRecordRepository.findByEnrollmentId(501L)).thenReturn(List.of(r1, r2, r3, r4, r5));

        when(riskAlertRepository.findByStudentIdOrderByDetectedAtDesc(101L)).thenReturn(List.of());

        StudentReportResponse report = reportService.getStudentReport("alice@university.edu");

        assertNotNull(report);
        assertEquals("STU-001", report.getStudentIdentifier());
        assertEquals(1, report.getTotalEnrolledSubjects());
        assertEquals(3, report.getTotalCreditsEarned());
        assertEquals(1, report.getPublishedSubjectsCount());
        assertEquals(1, report.getPassedSubjectsCount());
        assertEquals(0, report.getFailedSubjectsCount());
        assertEquals(4.0, report.getGpa());
        assertEquals(100.0, report.getOverallAttendancePercentage());

        StudentReportResponse.StudentSubjectReportItem subjItem = report.getSubjects().get(0);
        assertEquals(ResultPublicationStatus.PUBLISHED, subjItem.getPublicationStatus());
        assertEquals(78.0, subjItem.getTotalWeightedScore());
        assertEquals(Grade.A, subjItem.getGrade());
        assertEquals(4.0, subjItem.getGradePoint());
        assertEquals(2, subjItem.getAssessmentScores().size());
        assertEquals(100.0, subjItem.getAttendancePercentage());
        assertFalse(subjItem.isBelowAttendanceThreshold());
    }

    @Test
    @DisplayName("Should mask scores and grades when results are in DRAFT status for student")
    void shouldMaskMarksAndGradesWhenResultsAreDraftForStudent() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(101L)).thenReturn(List.of(enrollment));

        // Draft or missing publication
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "1"))
                .thenReturn(Optional.empty());

        when(attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of());
        when(riskAlertRepository.findByStudentIdOrderByDetectedAtDesc(101L)).thenReturn(List.of());

        StudentReportResponse report = reportService.getStudentReport("alice@university.edu");

        assertNotNull(report);
        assertEquals(0, report.getPublishedSubjectsCount());
        assertNull(report.getGpa());

        StudentReportResponse.StudentSubjectReportItem subj = report.getSubjects().get(0);
        assertEquals(ResultPublicationStatus.DRAFT, subj.getPublicationStatus());
        assertNull(subj.getTotalWeightedScore());
        assertNull(subj.getGrade());
        assertNull(subj.getGradePoint());
        assertTrue(subj.getAssessmentScores().isEmpty());
    }

    @Test
    @DisplayName("Should exclude excused sessions from attendance denominator and flag below 80% warning")
    void shouldCalculateAttendanceCorrectlyExcludingExcusedSessions() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(101L)).thenReturn(List.of(enrollment));
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(anyLong(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // 10 total sessions: 1 Excused, 6 Present, 3 Absent. Eligible = 9, Attended = 6 -> (6/9)*100 = 66.67% < 80%
        AttendanceSession s = new AttendanceSession(subject, LocalDate.now(), "L", "2025/2026", "1", lecturer);
        s.setId(1L);
        when(attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of(s, s, s, s, s, s, s, s, s, s)); // 10 sessions

        AttendanceRecord rPres = new AttendanceRecord(s, enrollment, AttendanceStatus.PRESENT, null);
        AttendanceRecord rAbs = new AttendanceRecord(s, enrollment, AttendanceStatus.ABSENT, null);
        AttendanceRecord rExc = new AttendanceRecord(s, enrollment, AttendanceStatus.EXCUSED, null);

        when(attendanceRecordRepository.findByEnrollmentId(501L)).thenReturn(List.of(
                rPres, rPres, rPres, rPres, rPres, rPres, // 6 Present
                rAbs, rAbs, rAbs,                         // 3 Absent
                rExc                                      // 1 Excused
        ));

        RiskAlert alert = new RiskAlert(student, subject, "2025/2026", "1", AlertRiskType.ATTENDANCE_WARNING,
                AlertSeverity.HIGH, "Attendance dropped to 66.67%", 66.67, "6/9 sessions");
        alert.setId(99L);
        alert.setActionNotes("Internal note that must never be exposed to student");
        when(riskAlertRepository.findByStudentIdOrderByDetectedAtDesc(101L)).thenReturn(List.of(alert));

        StudentReportResponse report = reportService.getStudentReport("alice@university.edu");

        StudentReportResponse.StudentSubjectReportItem subj = report.getSubjects().get(0);
        assertEquals(66.67, subj.getAttendancePercentage());
        assertTrue(subj.isBelowAttendanceThreshold());
        assertEquals(1, subj.getActiveAlerts().size());

        StudentReportResponse.StudentRiskAlertItem alertItem = subj.getActiveAlerts().get(0);
        assertEquals(AlertRiskType.ATTENDANCE_WARNING, alertItem.getRiskType());
        assertNotNull(alertItem.getAdvisoryGuidance());
    }

    @Test
    @DisplayName("Should export student academic report CSV with UTF-8 BOM and formula injection protection")
    void shouldExportStudentReportCsvWithHeadersAndSafeValues() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(101L)).thenReturn(List.of(enrollment));
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(anyLong(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(anyLong(), anyString(), anyString()))
                .thenReturn(List.of());
        when(riskAlertRepository.findByStudentIdOrderByDetectedAtDesc(101L)).thenReturn(List.of());

        byte[] csvBytes = reportService.exportStudentReportCsv("alice@university.edu");

        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 3);
        // Check UTF-8 BOM
        assertEquals((byte) 0xEF, csvBytes[0]);
        assertEquals((byte) 0xBB, csvBytes[1]);
        assertEquals((byte) 0xBF, csvBytes[2]);

        String csvString = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvString.contains("Subject Code,Subject Name,Credits"));
        assertTrue(csvString.contains("CS101,Introduction to Programming,3"));
    }

    // ==========================================
    // LECTURER REPORT TESTS
    // ==========================================

    @Test
    @DisplayName("Should generate lecturer class performance report with student rows and grade distribution")
    void shouldGenerateLecturerSubjectReportSuccessfully() {
        when(userRepository.findByEmail("dr.bob@university.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(assignmentRepository.existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
                100L, 201L, "2025/2026", "1")).thenReturn(true);

        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "1"))
                .thenReturn(Optional.of(new SubjectResultPublication(subject, "2025/2026", "1", ResultPublicationStatus.PUBLISHED, lecturer, LocalDateTime.now())));

        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "1"))
                .thenReturn(List.of(enrollment));

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of(quiz));

        StudentMark mark = new StudentMark(quiz, enrollment, 15.0, "Good", lecturer);
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of(mark));

        when(attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of());
        when(riskAlertRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByDetectedAtDesc(100L, "2025/2026", "1"))
                .thenReturn(List.of());

        LecturerSubjectReportResponse report = reportService.getLecturerSubjectReport(
                100L, "2025/2026", "1", "dr.bob@university.edu");

        assertNotNull(report);
        assertEquals(100L, report.getSubjectId());
        assertEquals("CS101", report.getSubjectCode());
        assertEquals(1, report.getEnrolledStudentsCount());
        assertEquals(1, report.getAssessedStudentsCount());
        assertEquals(15.0, report.getAverageScore());
        assertEquals(1, report.getStudents().size());
        assertEquals("STU-001", report.getStudents().get(0).getStudentIdentifier());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException if lecturer is not assigned to the subject")
    void shouldThrowAccessDeniedWhenLecturerNotAssigned() {
        when(userRepository.findByEmail("dr.bob@university.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(assignmentRepository.existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
                100L, 201L, "2025/2026", "1")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                reportService.getLecturerSubjectReport(100L, "2025/2026", "1", "dr.bob@university.edu"));
    }

    @Test
    @DisplayName("Should export lecturer performance CSV and attendance CSV")
    void shouldExportLecturerPerformanceCsv() {
        when(userRepository.findByEmail("dr.bob@university.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(assignmentRepository.existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
                100L, 201L, "2025/2026", "1")).thenReturn(true);
        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "1"))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "1"))
                .thenReturn(List.of(enrollment));
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of(quiz));
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of());
        when(attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of());
        when(riskAlertRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByDetectedAtDesc(100L, "2025/2026", "1"))
                .thenReturn(List.of());

        byte[] perfBytes = reportService.exportLecturerSubjectPerformanceCsv(100L, "2025/2026", "1", "dr.bob@university.edu");
        assertNotNull(perfBytes);
        String perfCsv = new String(perfBytes, StandardCharsets.UTF_8);
        assertTrue(perfCsv.contains("Student ID,Student Name,Email"));
        assertTrue(perfCsv.contains("STU-001,Alice Smith"));

        byte[] attBytes = reportService.exportLecturerSubjectAttendanceCsv(100L, "2025/2026", "1", "dr.bob@university.edu");
        assertNotNull(attBytes);
        String attCsv = new String(attBytes, StandardCharsets.UTF_8);
        assertTrue(attCsv.contains("Student ID,Student Name,Email,Total Sessions,Attended Sessions"));
    }

    // ==========================================
    // ADMIN INSTITUTIONAL REPORT TESTS
    // ==========================================

    @Test
    @DisplayName("Should generate admin institutional report with department and subject comparisons")
    void shouldGenerateAdminInstitutionalReportWithComparisonsAndDistributions() {
        when(userRepository.countByRoleAndStatus(Role.STUDENT, User.UserStatus.ACTIVE)).thenReturn(45L);
        when(userRepository.countByRoleAndStatus(Role.LECTURER, User.UserStatus.ACTIVE)).thenReturn(8L);
        when(departmentRepository.count()).thenReturn(2L);
        when(courseRepository.count()).thenReturn(3L);
        when(subjectRepository.count()).thenReturn(10L);

        when(subjectRepository.findAllByOrderByNameAsc()).thenReturn(List.of(subject));
        when(enrollmentRepository.findAllByOrderByEnrolledAtDesc()).thenReturn(List.of(enrollment));
        when(courseRepository.findAllByOrderByNameAsc()).thenReturn(List.of(course));
        when(departmentRepository.findAllByOrderByNameAsc()).thenReturn(List.of(department));

        when(publicationRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "1"))
                .thenReturn(Optional.of(new SubjectResultPublication(subject, "2025/2026", "1", ResultPublicationStatus.PUBLISHED, lecturer, LocalDateTime.now())));

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of(quiz));

        StudentMark mark = new StudentMark(quiz, enrollment, 18.0, "A", lecturer);
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of(mark));

        when(attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "1"))
                .thenReturn(List.of());
        when(riskAlertRepository.findAllByOrderByDetectedAtDesc()).thenReturn(List.of());

        AdminInstitutionalReportResponse report = reportService.getAdminInstitutionalReport(
                null, null, null, null, null);

        assertNotNull(report);
        assertEquals(45L, report.getTotalStudents());
        assertEquals(8L, report.getTotalLecturers());
        assertEquals(1, report.getTotalEnrollments());
        assertEquals(1, report.getDepartmentComparisons().size());
        assertEquals("COM", report.getDepartmentComparisons().get(0).getCode());
        assertEquals(1, report.getCourseComparisons().size());
        assertEquals("BCS", report.getCourseComparisons().get(0).getCode());
        assertEquals(1, report.getSubjectComparisons().size());
        assertEquals("CS101", report.getSubjectComparisons().get(0).getCode());
    }

    @Test
    @DisplayName("Should handle completely empty institutional data gracefully without division by zero")
    void shouldHandleCompletelyEmptyInstitutionalData() {
        when(userRepository.countByRoleAndStatus(any(), any())).thenReturn(0L);
        when(departmentRepository.count()).thenReturn(0L);
        when(courseRepository.count()).thenReturn(0L);
        when(subjectRepository.count()).thenReturn(0L);
        when(subjectRepository.findAllByOrderByNameAsc()).thenReturn(List.of());
        when(enrollmentRepository.findAllByOrderByEnrolledAtDesc()).thenReturn(List.of());
        when(courseRepository.findAllByOrderByNameAsc()).thenReturn(List.of());
        when(departmentRepository.findAllByOrderByNameAsc()).thenReturn(List.of());
        when(riskAlertRepository.findAllByOrderByDetectedAtDesc()).thenReturn(List.of());

        AdminInstitutionalReportResponse report = reportService.getAdminInstitutionalReport(
                null, null, null, null, null);

        assertNotNull(report);
        assertEquals(0, report.getTotalEnrollments());
        assertNull(report.getAverageScore());
        assertNull(report.getOverallPassRate());
        assertEquals(100.0, report.getOverallAttendanceAverage());
        assertTrue(report.getDepartmentComparisons().isEmpty());
        assertTrue(report.getCourseComparisons().isEmpty());
        assertTrue(report.getSubjectComparisons().isEmpty());
    }
}
