package com.sahrin.spas.service;

import com.sahrin.spas.dto.attendance.AttendanceSessionRequest;
import com.sahrin.spas.dto.attendance.AttendanceSessionResponse;
import com.sahrin.spas.dto.attendance.RecordAttendanceRequest;
import com.sahrin.spas.dto.attendance.StudentAttendanceSummaryResponse;
import com.sahrin.spas.entity.AttendanceRecord;
import com.sahrin.spas.entity.AttendanceSession;
import com.sahrin.spas.entity.AttendanceStatus;
import com.sahrin.spas.entity.Course;
import com.sahrin.spas.entity.Department;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.repository.AttendanceRecordRepository;
import com.sahrin.spas.repository.AttendanceSessionRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceSessionRepository sessionRepository;

    @Mock
    private AttendanceRecordRepository recordRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LecturerAssignmentRepository lecturerAssignmentRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private User lecturer;
    private User student1;
    private Department department;
    private Course course;
    private Subject subject;

    @BeforeEach
    void setUp() {
        lecturer = new User("Dr. Smith", "LEC-001", "smith@spas.edu", "hash", Role.LECTURER, UserStatus.ACTIVE);
        lecturer.setId(10L);

        student1 = new User("Alice", "STU-001", "alice@spas.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student1.setId(21L);

        department = new Department("CS", "Computer Science", "Desc");
        department.setId(1L);

        course = new Course("BCS", "Bachelor of CS", "Desc", department);
        course.setId(10L);

        subject = new Subject("CS101", "Intro to Programming", "Desc", 3, 1, course, department);
        subject.setId(100L);
    }

    @Test
    @DisplayName("Lecturer assigned to subject creates attendance session successfully")
    void testCreateSessionSuccess() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);

        AttendanceSession session = new AttendanceSession(subject, LocalDate.now(), "Week 1 Lecture",
                "2025/2026", "SEMESTER_1", lecturer);
        session.setId(1L);
        when(sessionRepository.save(any(AttendanceSession.class))).thenReturn(session);

        AttendanceSessionRequest req = new AttendanceSessionRequest(
                100L, LocalDate.now(), "Week 1 Lecture", "2025/2026", "SEMESTER_1");
        AttendanceSessionResponse resp = attendanceService.createSession(req, "smith@spas.edu");

        assertNotNull(resp);
        assertEquals("Week 1 Lecture", resp.getSessionTopic());
        verify(sessionRepository).save(any(AttendanceSession.class));
    }

    @Test
    @DisplayName("Lecturer not assigned to subject cannot create attendance session")
    void testCreateSessionUnauthorized() {
        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(false);

        AttendanceSessionRequest req = new AttendanceSessionRequest(
                100L, LocalDate.now(), "Week 1 Lecture", "2025/2026", "SEMESTER_1");

        assertThrows(AccessDeniedException.class, () -> attendanceService.createSession(req, "smith@spas.edu"));
    }

    @Test
    @DisplayName("Record attendance rejects students not enrolled in the subject")
    void testRecordAttendanceRejectsUnenrolled() {
        AttendanceSession session = new AttendanceSession(subject, LocalDate.now(), "Week 1",
                "2025/2026", "SEMESTER_1", lecturer);
        session.setId(1L);

        Enrollment enrollment = new Enrollment(student1, course, subject, "2025/2026", "SEMESTER_1");
        enrollment.setId(200L);
        enrollment.setStatus(EnrollmentStatus.DROPPED);

        when(userRepository.findByEmail("smith@spas.edu")).thenReturn(Optional.of(lecturer));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(100L, 10L)).thenReturn(true);
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(enrollment));

        RecordAttendanceRequest req = new RecordAttendanceRequest(
                1L, List.of(new RecordAttendanceRequest.StudentAttendanceItem(200L, AttendanceStatus.PRESENT, "Present")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attendanceService.recordSessionAttendance(req, "smith@spas.edu"));
        assertTrue(ex.getMessage().contains("non-active"));
    }

    @Test
    @DisplayName("Attendance formula: excludes EXCUSED from denominator; PRESENT+LATE in numerator")
    void testAttendanceCalculationWithExcused() {
        Enrollment e1 = new Enrollment(student1, course, subject, "2025/2026", "SEMESTER_1");
        e1.setId(200L);

        AttendanceSession s1 = new AttendanceSession(subject, LocalDate.now(), "S1", "2025/2026", "SEMESTER_1", lecturer);
        s1.setId(1L);
        AttendanceSession s2 = new AttendanceSession(subject, LocalDate.now(), "S2", "2025/2026", "SEMESTER_1", lecturer);
        s2.setId(2L);
        AttendanceSession s3 = new AttendanceSession(subject, LocalDate.now(), "S3", "2025/2026", "SEMESTER_1", lecturer);
        s3.setId(3L);
        AttendanceSession s4 = new AttendanceSession(subject, LocalDate.now(), "S4", "2025/2026", "SEMESTER_1", lecturer);
        s4.setId(4L);

        // Student has: 2 PRESENT, 1 LATE, 1 EXCUSED.
        // Total = 4, Excused = 1 -> Eligible = 3.
        // Attended = PRESENT(2) + LATE(1) = 3.
        // Percentage = 3 / 3 * 100 = 100.0%.
        AttendanceRecord r1 = new AttendanceRecord(s1, e1, AttendanceStatus.PRESENT, "On time");
        AttendanceRecord r2 = new AttendanceRecord(s2, e1, AttendanceStatus.PRESENT, "On time");
        AttendanceRecord r3 = new AttendanceRecord(s3, e1, AttendanceStatus.LATE, "10 min late");
        AttendanceRecord r4 = new AttendanceRecord(s4, e1, AttendanceStatus.EXCUSED, "Doctor note");

        when(userRepository.findByEmail("alice@spas.edu")).thenReturn(Optional.of(student1));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(e1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                100L, "2025/2026", "SEMESTER_1")).thenReturn(List.of(s1, s2, s3, s4));
        when(recordRepository.findByEnrollmentId(200L)).thenReturn(List.of(r1, r2, r3, r4));

        StudentAttendanceSummaryResponse summary = attendanceService.getMySubjectAttendance(200L, "alice@spas.edu");

        assertEquals(4, summary.getTotalSessions());
        assertEquals(1, summary.getExcusedSessions());
        assertEquals(3, summary.getEligibleSessions());
        assertEquals(3, summary.getAttendedSessions());
        assertEquals(0, summary.getAbsentSessions());
        assertEquals(100.0, summary.getAttendancePercentage());
        assertFalse(summary.isBelowWarningThreshold());
    }

    @Test
    @DisplayName("Strict < 80.0% threshold triggers warning; exactly 80.0% does not trigger warning")
    void testWarningThresholdStrictness() {
        Enrollment e1 = new Enrollment(student1, course, subject, "2025/2026", "SEMESTER_1");
        e1.setId(200L);

        // 5 sessions total, no excused -> 5 eligible.
        // 4 attended (PRESENT), 1 ABSENT -> 4 / 5 = 80.0%.
        AttendanceSession s1 = new AttendanceSession(subject, LocalDate.now(), "S1", "2025/2026", "SEMESTER_1", lecturer);
        s1.setId(1L);
        AttendanceSession s2 = new AttendanceSession(subject, LocalDate.now(), "S2", "2025/2026", "SEMESTER_1", lecturer);
        s2.setId(2L);
        AttendanceSession s3 = new AttendanceSession(subject, LocalDate.now(), "S3", "2025/2026", "SEMESTER_1", lecturer);
        s3.setId(3L);
        AttendanceSession s4 = new AttendanceSession(subject, LocalDate.now(), "S4", "2025/2026", "SEMESTER_1", lecturer);
        s4.setId(4L);
        AttendanceSession s5 = new AttendanceSession(subject, LocalDate.now(), "S5", "2025/2026", "SEMESTER_1", lecturer);
        s5.setId(5L);

        AttendanceRecord r1 = new AttendanceRecord(s1, e1, AttendanceStatus.PRESENT, "");
        AttendanceRecord r2 = new AttendanceRecord(s2, e1, AttendanceStatus.PRESENT, "");
        AttendanceRecord r3 = new AttendanceRecord(s3, e1, AttendanceStatus.PRESENT, "");
        AttendanceRecord r4 = new AttendanceRecord(s4, e1, AttendanceStatus.PRESENT, "");
        AttendanceRecord r5 = new AttendanceRecord(s5, e1, AttendanceStatus.ABSENT, "");

        when(userRepository.findByEmail("alice@spas.edu")).thenReturn(Optional.of(student1));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(e1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                100L, "2025/2026", "SEMESTER_1")).thenReturn(List.of(s1, s2, s3, s4, s5));
        when(recordRepository.findByEnrollmentId(200L)).thenReturn(List.of(r1, r2, r3, r4, r5));

        StudentAttendanceSummaryResponse summary80 = attendanceService.getMySubjectAttendance(200L, "alice@spas.edu");

        assertEquals(80.0, summary80.getAttendancePercentage());
        assertFalse(summary80.isBelowWarningThreshold(), "Exactly 80.0% should NOT trigger warning");

        // Now test 3 attended out of 4 (75.0%) -> STRICTLY < 80.0% triggers warning
        AttendanceRecord r4Absent = new AttendanceRecord(s4, e1, AttendanceStatus.ABSENT, "");
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                100L, "2025/2026", "SEMESTER_1")).thenReturn(List.of(s1, s2, s3, s4));
        when(recordRepository.findByEnrollmentId(200L)).thenReturn(List.of(r1, r2, r3, r4Absent));

        StudentAttendanceSummaryResponse summary75 = attendanceService.getMySubjectAttendance(200L, "alice@spas.edu");

        assertEquals(75.0, summary75.getAttendancePercentage());
        assertTrue(summary75.isBelowWarningThreshold(), "75.0% should trigger warning banner");
    }

    @Test
    @DisplayName("Zero sessions handled gracefully without division by zero")
    void testZeroSessionsNoDivideByZero() {
        Enrollment e1 = new Enrollment(student1, course, subject, "2025/2026", "SEMESTER_1");
        e1.setId(200L);

        when(userRepository.findByEmail("alice@spas.edu")).thenReturn(Optional.of(student1));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(e1));
        when(sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                100L, "2025/2026", "SEMESTER_1")).thenReturn(List.of());
        when(recordRepository.findByEnrollmentId(200L)).thenReturn(List.of());

        StudentAttendanceSummaryResponse summary = attendanceService.getMySubjectAttendance(200L, "alice@spas.edu");

        assertEquals(0, summary.getTotalSessions());
        assertEquals(0, summary.getEligibleSessions());
        assertEquals(100.0, summary.getAttendancePercentage());
        assertFalse(summary.isBelowWarningThreshold());
    }
}
