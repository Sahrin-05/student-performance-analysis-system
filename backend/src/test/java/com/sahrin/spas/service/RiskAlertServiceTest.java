package com.sahrin.spas.service;

import com.sahrin.spas.config.RiskAlertProperties;
import com.sahrin.spas.dto.risk.AlertActionRequest;
import com.sahrin.spas.dto.risk.AlertResolutionRequest;
import com.sahrin.spas.dto.risk.AlertReviewRequest;
import com.sahrin.spas.dto.risk.RiskAlertResponse;
import com.sahrin.spas.dto.risk.StudentRiskAlertResponse;
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
import com.sahrin.spas.entity.RiskAlert;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.StudentMark;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.repository.AssessmentRepository;
import com.sahrin.spas.repository.AttendanceRecordRepository;
import com.sahrin.spas.repository.AttendanceSessionRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.RiskAlertRepository;
import com.sahrin.spas.repository.StudentMarkRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.RiskAlertServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAlertServiceTest {

    @Mock
    private RiskAlertRepository riskAlertRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LecturerAssignmentRepository lecturerAssignmentRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private StudentMarkRepository studentMarkRepository;

    @Mock
    private AttendanceSessionRepository sessionRepository;

    @Mock
    private AttendanceRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private RiskAlertProperties riskProperties = new RiskAlertProperties(80.0, 40.0, 15.0);

    @InjectMocks
    private RiskAlertServiceImpl riskAlertService;

    private User admin;
    private User lecturer;
    private User student1;
    private User student2;
    private Subject subject;
    private Enrollment enrollment1;
    private Enrollment enrollment2;

    @BeforeEach
    void setUp() {
        admin = new User("Admin", "ADM-001", "admin@spas.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        lecturer = new User("Dr. Smith", "LEC-001", "smith@spas.edu", "hash", Role.LECTURER, UserStatus.ACTIVE);
        lecturer.setId(10L);

        student1 = new User("Alice", "STU-001", "alice@spas.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student1.setId(21L);

        student2 = new User("Bob", "STU-002", "bob@spas.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student2.setId(22L);

        Department dept = new Department("CS", "Computer Science", "Desc");
        dept.setId(1L);
        Course course = new Course("BCS", "Bachelor of CS", "Desc", dept);
        course.setId(2L);
        subject = new Subject("CS101", "Programming 101", "Intro", 3, 1, course, dept);
        subject.setId(100L);

        enrollment1 = new Enrollment(student1, course, subject, "2025/2026", "SEMESTER_1");
        enrollment1.setId(501L);
        enrollment1.setStatus(EnrollmentStatus.ACTIVE);

        enrollment2 = new Enrollment(student2, course, subject, "2025/2026", "SEMESTER_1");
        enrollment2.setId(502L);
        enrollment2.setStatus(EnrollmentStatus.ACTIVE);
    }

    // =========================================================================
    // 1. ATTENDANCE DETECTION RULE TESTS
    // =========================================================================

    @Test
    @DisplayName("Attendance below 80% (e.g. 70.0%) triggers ATTENDANCE_WARNING alert with HIGH severity")
    void testAttendanceBelow80TriggersAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));

        // Create 10 sessions
        List<AttendanceSession> sessions = new ArrayList<>();
        List<AttendanceRecord> records = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            AttendanceSession s = new AttendanceSession(subject, LocalDate.now().minusDays(i), "Topic " + i, "2025/2026", "SEMESTER_1", lecturer);
            s.setId((long) i);
            sessions.add(s);

            // Alice attended 7 sessions, absent for 3
            AttendanceStatus st = i <= 7 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
            AttendanceRecord r = new AttendanceRecord(s, enrollment1, st, null);
            records.add(r);
        }

        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(sessions);
        when(recordRepository.findBySessionIdIn(any())).thenReturn(records);
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());
        when(riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                eq(21L), eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq(AlertRiskType.ATTENDANCE_WARNING), eq(AlertStatus.RESOLVED)))
                .thenReturn(false);

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        ArgumentCaptor<RiskAlert> captor = ArgumentCaptor.forClass(RiskAlert.class);
        verify(riskAlertRepository).save(captor.capture());

        RiskAlert saved = captor.getValue();
        assertEquals(AlertRiskType.ATTENDANCE_WARNING, saved.getRiskType());
        assertEquals(AlertSeverity.HIGH, saved.getSeverity());
        assertEquals(70.0, saved.getMetricValue());
        assertTrue(saved.getReason().contains("70.00% is below the provisional warning threshold"));
    }

    @Test
    @DisplayName("Attendance below 60% (e.g. 50.0%) triggers ATTENDANCE_WARNING alert with CRITICAL severity")
    void testAttendanceBelow60TriggersCriticalAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));

        List<AttendanceSession> sessions = new ArrayList<>();
        List<AttendanceRecord> records = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            AttendanceSession s = new AttendanceSession(subject, LocalDate.now().minusDays(i), "Topic " + i, "2025/2026", "SEMESTER_1", lecturer);
            s.setId((long) i);
            sessions.add(s);

            AttendanceStatus st = i <= 5 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
            AttendanceRecord r = new AttendanceRecord(s, enrollment1, st, null);
            records.add(r);
        }

        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(sessions);
        when(recordRepository.findBySessionIdIn(any())).thenReturn(records);
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());
        when(riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                eq(21L), eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq(AlertRiskType.ATTENDANCE_WARNING), eq(AlertStatus.RESOLVED)))
                .thenReturn(false);

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        ArgumentCaptor<RiskAlert> captor = ArgumentCaptor.forClass(RiskAlert.class);
        verify(riskAlertRepository).save(captor.capture());
        assertEquals(AlertSeverity.CRITICAL, captor.getValue().getSeverity());
    }

    @Test
    @DisplayName("Attendance exactly 80.0% generates NO alert (strict boundary condition)")
    void testAttendanceExactly80GeneratesNoAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));

        // 10 sessions, attended 8 -> 80.00%
        List<AttendanceSession> sessions = new ArrayList<>();
        List<AttendanceRecord> records = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            AttendanceSession s = new AttendanceSession(subject, LocalDate.now().minusDays(i), "Topic " + i, "2025/2026", "SEMESTER_1", lecturer);
            s.setId((long) i);
            sessions.add(s);

            AttendanceStatus st = i <= 8 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
            records.add(new AttendanceRecord(s, enrollment1, st, null));
        }

        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(sessions);
        when(recordRepository.findBySessionIdIn(any())).thenReturn(records);
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("Attendance with Excused sessions excludes excused from denominator")
    void testAttendanceExcusedSessionsExcludedFromDenominator() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));

        // 5 total sessions: 1 excused, 3 attended, 1 absent -> eligible = 4. 3 / 4 = 75.0% < 80.0% -> alert!
        List<AttendanceSession> sessions = new ArrayList<>();
        List<AttendanceRecord> records = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            AttendanceSession s = new AttendanceSession(subject, LocalDate.now().minusDays(i), "Topic " + i, "2025/2026", "SEMESTER_1", lecturer);
            s.setId((long) i);
            sessions.add(s);

            AttendanceStatus st;
            if (i == 1) st = AttendanceStatus.EXCUSED;
            else if (i <= 4) st = AttendanceStatus.PRESENT;
            else st = AttendanceStatus.ABSENT;

            records.add(new AttendanceRecord(s, enrollment1, st, null));
        }

        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(sessions);
        when(recordRepository.findBySessionIdIn(any())).thenReturn(records);
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());
        when(riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                eq(21L), eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq(AlertRiskType.ATTENDANCE_WARNING), eq(AlertStatus.RESOLVED)))
                .thenReturn(false);

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        ArgumentCaptor<RiskAlert> captor = ArgumentCaptor.forClass(RiskAlert.class);
        verify(riskAlertRepository).save(captor.capture());
        assertEquals(75.0, captor.getValue().getMetricValue());
    }

    @Test
    @DisplayName("Zero eligible sessions generates NO alert (divide-by-zero protection)")
    void testZeroEligibleSessionsGeneratesNoAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));

        // 2 sessions, both EXCUSED -> eligible = 0
        AttendanceSession s1 = new AttendanceSession(subject, LocalDate.now(), "T1", "2025/2026", "SEMESTER_1", lecturer);
        s1.setId(1L);
        AttendanceRecord r1 = new AttendanceRecord(s1, enrollment1, AttendanceStatus.EXCUSED, null);

        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(s1));
        when(recordRepository.findBySessionIdIn(any())).thenReturn(List.of(r1));
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
    }

    // =========================================================================
    // 2. LOW ACADEMIC MARKS RULE TESTS
    // =========================================================================

    @Test
    @DisplayName("Cumulative marks below 40.0% triggers LOW_ACADEMIC_MARKS alert")
    void testLowMarksBelow40TriggersAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        Assessment quiz = new Assessment("Quiz 1", AssessmentType.QUIZ, 100.0, 40.0, subject, "2025/2026", "SEMESTER_1", lecturer);
        quiz.setId(1L);

        // Student scored 30 / 100 on Quiz 1 -> 30.0% < 40.0%
        StudentMark mark = new StudentMark(quiz, enrollment1, 30.0, null, lecturer);
        mark.setId(10L);

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(quiz));
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of(mark));
        when(riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                eq(21L), eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq(AlertRiskType.LOW_ACADEMIC_MARKS), eq(AlertStatus.RESOLVED)))
                .thenReturn(false);

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        ArgumentCaptor<RiskAlert> captor = ArgumentCaptor.forClass(RiskAlert.class);
        verify(riskAlertRepository).save(captor.capture());

        RiskAlert alert = captor.getValue();
        assertEquals(AlertRiskType.LOW_ACADEMIC_MARKS, alert.getRiskType());
        assertEquals(30.0, alert.getMetricValue());
        assertEquals(AlertSeverity.HIGH, alert.getSeverity());
    }

    @Test
    @DisplayName("Cumulative marks at or above 40.0% generates NO low marks alert")
    void testLowMarksAtOrAbove40GeneratesNoAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        Assessment quiz = new Assessment("Quiz 1", AssessmentType.QUIZ, 100.0, 40.0, subject, "2025/2026", "SEMESTER_1", lecturer);
        quiz.setId(1L);

        // Student scored 60 / 100 -> 60.0% >= 40.0%
        StudentMark mark = new StudentMark(quiz, enrollment1, 60.0, null, lecturer);

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(quiz));
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of(mark));

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
    }

    // =========================================================================
    // 3. ACADEMIC PROGRESS DECLINE RULE TESTS
    // =========================================================================

    private void setAssessmentCreatedAt(Assessment assessment, LocalDateTime time) {
        try {
            Field field = Assessment.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(assessment, time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Decline of >= 15.0% between sequential assessments triggers PROGRESS_DECLINE alert")
    void testProgressDeclineDrop15OrMoreTriggersAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        Assessment ass1 = new Assessment("Quiz 1", AssessmentType.QUIZ, 100.0, 20.0, subject, "2025/2026", "SEMESTER_1", lecturer);
        ass1.setId(1L);
        setAssessmentCreatedAt(ass1, LocalDateTime.now().minusDays(10));

        Assessment ass2 = new Assessment("Midterm", AssessmentType.MIDTERM, 100.0, 30.0, subject, "2025/2026", "SEMESTER_1", lecturer);
        ass2.setId(2L);
        setAssessmentCreatedAt(ass2, LocalDateTime.now().minusDays(2));

        // Alice scored 85% on Quiz 1, but 65% on Midterm -> Drop is 20.0% >= 15.0%
        StudentMark m1 = new StudentMark(ass1, enrollment1, 85.0, null, lecturer);
        StudentMark m2 = new StudentMark(ass2, enrollment1, 65.0, null, lecturer);

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(ass1, ass2));
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of(m1, m2));
        when(riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                eq(21L), eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq(AlertRiskType.PROGRESS_DECLINE), eq(AlertStatus.RESOLVED)))
                .thenReturn(false);

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        ArgumentCaptor<RiskAlert> captor = ArgumentCaptor.forClass(RiskAlert.class);
        verify(riskAlertRepository).save(captor.capture());

        RiskAlert alert = captor.getValue();
        assertEquals(AlertRiskType.PROGRESS_DECLINE, alert.getRiskType());
        assertEquals(20.0, alert.getMetricValue());
        assertTrue(alert.getReason().contains("declined by 20.00%"));
    }

    @Test
    @DisplayName("Decline with < 2 graded assessments generates NO alert (insufficient data guard)")
    void testProgressDeclineWithSingleAssessmentGeneratesNoAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        Assessment ass1 = new Assessment("Quiz 1", AssessmentType.QUIZ, 100.0, 20.0, subject, "2025/2026", "SEMESTER_1", lecturer);
        ass1.setId(1L);
        setAssessmentCreatedAt(ass1, LocalDateTime.now().minusDays(5));
        StudentMark m1 = new StudentMark(ass1, enrollment1, 50.0, null, lecturer);

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(ass1));
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of(m1));

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        // No decline alert should be created because size < 2
        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
    }

    // =========================================================================
    // 4. MISSING ASSESSMENT RULE TESTS
    // =========================================================================

    @Test
    @DisplayName("Missing assessment flags active student when peer marks exist")
    void testMissingAssessmentWhenPeersHaveMarksTriggersAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1, enrollment2));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        Assessment quiz = new Assessment("Quiz 1", AssessmentType.QUIZ, 100.0, 20.0, subject, "2025/2026", "SEMESTER_1", lecturer);
        quiz.setId(10L);

        // Bob (student 2) received a mark, but Alice (student 1) has no mark
        StudentMark bobMark = new StudentMark(quiz, enrollment2, 80.0, null, lecturer);

        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(quiz));
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L, 502L))).thenReturn(List.of(bobMark));
        when(riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                eq(21L), eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq(AlertRiskType.MISSING_ASSESSMENT), eq(AlertStatus.RESOLVED)))
                .thenReturn(false);

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        ArgumentCaptor<RiskAlert> captor = ArgumentCaptor.forClass(RiskAlert.class);
        verify(riskAlertRepository).save(captor.capture());

        RiskAlert alert = captor.getValue();
        assertEquals(AlertRiskType.MISSING_ASSESSMENT, alert.getRiskType());
        assertEquals(student1.getId(), alert.getStudent().getId());
        assertTrue(alert.getReason().contains("Missing required mark"));
    }

    @Test
    @DisplayName("Missing assessment does NOT flag when NO peers have received marks yet (insufficient data guard)")
    void testMissingAssessmentWhenNoPeersHaveMarksGeneratesNoAlert() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        Assessment quiz = new Assessment("Quiz 1", AssessmentType.QUIZ, 100.0, 20.0, subject, "2025/2026", "SEMESTER_1", lecturer);
        quiz.setId(10L);

        // Nobody has any mark recorded for Quiz 1 yet
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(quiz));
        when(studentMarkRepository.findByEnrollmentIdIn(List.of(501L))).thenReturn(List.of());

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
    }

    // =========================================================================
    // 5. DEDUPLICATION AND LIFECYCLE TESTS
    // =========================================================================

    @Test
    @DisplayName("Duplicate active alert is prevented if non-resolved alert already exists")
    void testDuplicateActiveAlertPrevented() {
        when(userRepository.findByEmail("admin@spas.edu")).thenReturn(Optional.of(admin));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(enrollment1));

        AttendanceSession s1 = new AttendanceSession(subject, LocalDate.now(), "T1", "2025/2026", "SEMESTER_1", lecturer);
        s1.setId(1L);
        AttendanceRecord r1 = new AttendanceRecord(s1, enrollment1, AttendanceStatus.ABSENT, null);

        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of(s1));
        when(recordRepository.findBySessionIdIn(any())).thenReturn(List.of(r1));
        when(assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(100L, "2025/2026", "SEMESTER_1"))
                .thenReturn(List.of());

        // Already active in database!
        when(riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                eq(21L), eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq(AlertRiskType.ATTENDANCE_WARNING), eq(AlertStatus.RESOLVED)))
                .thenReturn(true);

        riskAlertService.evaluateSubjectRisk(100L, "2025/2026", "SEMESTER_1", "admin@spas.edu");

        // Must not save duplicate
        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("Lifecycle transition: DETECTED -> REVIEWED -> ACTION_RECORDED -> RESOLVED")
    void testLifecycleTransitions() {
        RiskAlert alert = new RiskAlert(student1, subject, "2025/2026", "SEMESTER_1",
                AlertRiskType.ATTENDANCE_WARNING, AlertSeverity.HIGH, "Attendance low", 70.0, "details");
        alert.setId(99L);
        assertEquals(AlertStatus.DETECTED, alert.getStatus());

        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(riskAlertRepository.findById(99L)).thenReturn(Optional.of(alert));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Review
        RiskAlertResponse revResp = riskAlertService.reviewAlert(99L, new AlertReviewRequest("Reviewed by lecturer"), "smith@spas.edu");
        assertEquals(AlertStatus.REVIEWED, revResp.getStatus());
        assertEquals("Dr. Smith", revResp.getReviewedByName());

        // 2. Action Recorded
        RiskAlertResponse actResp = riskAlertService.recordAction(99L, new AlertActionRequest("Arranged 1-on-1 counseling session with student"), "smith@spas.edu");
        assertEquals(AlertStatus.ACTION_RECORDED, actResp.getStatus());
        assertEquals("Arranged 1-on-1 counseling session with student", actResp.getActionNotes());

        // 3. Resolve
        RiskAlertResponse resResp = riskAlertService.resolveAlert(99L, new AlertResolutionRequest("Student attended counseling and improved attendance to 85%"), "smith@spas.edu");
        assertEquals(AlertStatus.RESOLVED, resResp.getStatus());
        assertEquals("Student attended counseling and improved attendance to 85%", resResp.getResolutionNotes());
        assertEquals("Dr. Smith", resResp.getResolvedByName());
    }

    @Test
    @DisplayName("Lecturer cannot access or manage alerts for unassigned subjects (403 AccessDeniedException)")
    void testLecturerUnassignedSubjectForbidden() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                riskAlertService.getLecturerAlertsForSubject(100L, "2025/2026", "SEMESTER_1", "smith@spas.edu"));
    }

    @Test
    @DisplayName("Student personal alerts view strictly hides internal faculty commentary")
    void testStudentGetsSanitizedAlertsWithoutStaffNotes() {
        RiskAlert alert = new RiskAlert(student1, subject, "2025/2026", "SEMESTER_1",
                AlertRiskType.ATTENDANCE_WARNING, AlertSeverity.HIGH, "Attendance low", 70.0, "details");
        alert.setId(99L);
        alert.setStatus(AlertStatus.ACTION_RECORDED);
        alert.setReviewedBy(lecturer);
        alert.setReviewedAt(LocalDateTime.now());
        alert.setActionNotes("Confidential faculty intervention remarks.");
        alert.setActionRecordedBy(lecturer);

        when(userRepository.findByEmail("alice@spas.edu")).thenReturn(Optional.of(student1));
        when(riskAlertRepository.findByStudentIdOrderByDetectedAtDesc(21L)).thenReturn(List.of(alert));

        List<StudentRiskAlertResponse> studentAlerts = riskAlertService.getMyAlerts("alice@spas.edu");

        assertEquals(1, studentAlerts.size());
        StudentRiskAlertResponse resp = studentAlerts.get(0);
        assertEquals(AlertRiskType.ATTENDANCE_WARNING, resp.getRiskType());
        assertEquals("CS101", resp.getSubjectCode());
        assertNotNull(resp.getAdvisoryGuidance());
        assertTrue(resp.getAdvisoryGuidance().contains("Your attendance is currently below the 80% threshold"));
        // Notice: StudentRiskAlertResponse does not contain actionNotes or staff reviewer IDs!
    }
}
