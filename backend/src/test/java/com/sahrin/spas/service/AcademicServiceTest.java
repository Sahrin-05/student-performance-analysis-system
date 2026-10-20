package com.sahrin.spas.service;

import com.sahrin.spas.dto.academic.CourseRequest;
import com.sahrin.spas.dto.academic.CourseResponse;
import com.sahrin.spas.dto.academic.DepartmentRequest;
import com.sahrin.spas.dto.academic.DepartmentResponse;
import com.sahrin.spas.dto.academic.EnrollmentRequest;
import com.sahrin.spas.dto.academic.EnrollmentResponse;
import com.sahrin.spas.dto.academic.LecturerAssignmentRequest;
import com.sahrin.spas.dto.academic.LecturerAssignmentResponse;
import com.sahrin.spas.dto.academic.SubjectRequest;
import com.sahrin.spas.dto.academic.SubjectResponse;
import com.sahrin.spas.entity.Course;
import com.sahrin.spas.entity.Department;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.LecturerAssignment;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.DuplicateResourceException;
import com.sahrin.spas.exception.InvalidStatusTransitionException;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.CourseRepository;
import com.sahrin.spas.repository.DepartmentRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.AcademicServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
class AcademicServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private LecturerAssignmentRepository lecturerAssignmentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AcademicServiceImpl academicService;

    private Department dept;
    private Course course;
    private Subject subject;
    private User lecturer;
    private User student;

    @BeforeEach
    void setUp() {
        dept = new Department("CS", "Computer Science", "Department of Computer Science");
        dept.setId(1L);

        course = new Course("BCS", "Bachelor of Computer Science", "4-year degree", dept);
        course.setId(10L);

        subject = new Subject("CS101", "Programming Fundamentals", "Intro course", 3, 1, course, dept);
        subject.setId(100L);

        lecturer = new User("Dr. Alice", "LEC-01", "alice@university.edu", "hash", Role.LECTURER, UserStatus.ACTIVE);
        lecturer.setId(50L);

        student = new User("Bob Smith", "STU-01", "bob@student.university.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student.setId(60L);
    }

    // ==========================================
    // DEPARTMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Should successfully create a department")
    void createDepartment_success() {
        when(departmentRepository.existsByCodeIgnoreCase("ENG")).thenReturn(false);
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> {
            Department d = i.getArgument(0);
            d.setId(2L);
            return d;
        });

        DepartmentRequest req = new DepartmentRequest("ENG", "Engineering", "Engineering Department");
        DepartmentResponse res = academicService.createDepartment(req);

        assertNotNull(res);
        assertEquals("ENG", res.getCode());
        assertEquals("Engineering", res.getName());
        assertTrue(res.isActive());
    }

    @Test
    @DisplayName("Should reject department with duplicate code")
    void createDepartment_duplicateCode_throwsException() {
        when(departmentRepository.existsByCodeIgnoreCase("CS")).thenReturn(true);

        DepartmentRequest req = new DepartmentRequest("CS", "Computing", "Desc");
        assertThrows(DuplicateResourceException.class, () -> academicService.createDepartment(req));
    }

    @Test
    @DisplayName("Should reject deactivating department with active courses")
    void setDepartmentStatus_activeCourses_throwsException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(courseRepository.existsByDepartmentIdAndActiveTrue(1L)).thenReturn(true);

        assertThrows(InvalidStatusTransitionException.class, () -> academicService.setDepartmentStatus(1L, false));
    }

    @Test
    @DisplayName("Should deactivate department without active courses")
    void setDepartmentStatus_noActiveCourses_success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(courseRepository.existsByDepartmentIdAndActiveTrue(1L)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));

        DepartmentResponse res = academicService.setDepartmentStatus(1L, false);
        assertFalse(res.isActive());
    }

    // ==========================================
    // COURSE TESTS
    // ==========================================

    @Test
    @DisplayName("Should successfully create a course")
    void createCourse_success() {
        when(courseRepository.existsByCodeIgnoreCase("BIT")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> {
            Course c = i.getArgument(0);
            c.setId(20L);
            return c;
        });

        CourseRequest req = new CourseRequest("BIT", "Bachelor of IT", "IT degree", 1L);
        CourseResponse res = academicService.createCourse(req);

        assertNotNull(res);
        assertEquals("BIT", res.getCode());
        assertEquals("CS", res.getDepartmentCode());
    }

    @Test
    @DisplayName("Should reject course creation with inactive department")
    void createCourse_inactiveDepartment_throwsException() {
        dept.setActive(false);
        when(courseRepository.existsByCodeIgnoreCase("BIT")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));

        CourseRequest req = new CourseRequest("BIT", "Bachelor of IT", "IT degree", 1L);
        assertThrows(InvalidStatusTransitionException.class, () -> academicService.createCourse(req));
    }

    // ==========================================
    // SUBJECT TESTS
    // ==========================================

    @Test
    @DisplayName("Should successfully create a subject")
    void createSubject_success() {
        when(subjectRepository.existsByCodeIgnoreCase("CS102")).thenReturn(false);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(subjectRepository.save(any(Subject.class))).thenAnswer(i -> {
            Subject s = i.getArgument(0);
            s.setId(102L);
            return s;
        });

        SubjectRequest req = new SubjectRequest("CS102", "OOP", "Object Oriented Programming", 4, 2, 10L);
        SubjectResponse res = academicService.createSubject(req);

        assertNotNull(res);
        assertEquals("CS102", res.getCode());
        assertEquals(4, res.getCredits());
        assertEquals(10L, res.getCourseId());
        assertEquals(1L, res.getDepartmentId());
    }

    @Test
    @DisplayName("Should reject subject creation with inactive course")
    void createSubject_inactiveCourse_throwsException() {
        course.setActive(false);
        when(subjectRepository.existsByCodeIgnoreCase("CS102")).thenReturn(false);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        SubjectRequest req = new SubjectRequest("CS102", "OOP", "Desc", 4, 2, 10L);
        assertThrows(InvalidStatusTransitionException.class, () -> academicService.createSubject(req));
    }

    // ==========================================
    // LECTURER ASSIGNMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Should successfully assign an active lecturer to an active subject")
    void assignLecturer_success() {
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(userRepository.findById(50L)).thenReturn(Optional.of(lecturer));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
                100L, 50L, "2025/2026", "Semester 1")).thenReturn(false);
        when(lecturerAssignmentRepository.save(any(LecturerAssignment.class))).thenAnswer(i -> {
            LecturerAssignment a = i.getArgument(0);
            a.setId(1L);
            return a;
        });

        LecturerAssignmentRequest req = new LecturerAssignmentRequest(100L, 50L, "2025/2026", "Semester 1");
        LecturerAssignmentResponse res = academicService.assignLecturer(req);

        assertNotNull(res);
        assertEquals("CS101", res.getSubjectCode());
        assertEquals("Dr. Alice", res.getLecturerName());
        assertTrue(res.isActive());
    }

    @Test
    @DisplayName("Should reject assigning non-lecturer user to subject")
    void assignLecturer_nonLecturer_throwsException() {
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(userRepository.findById(60L)).thenReturn(Optional.of(student));

        LecturerAssignmentRequest req = new LecturerAssignmentRequest(100L, 60L, "2025/2026", "Semester 1");
        assertThrows(InvalidStatusTransitionException.class, () -> academicService.assignLecturer(req));
    }

    @Test
    @DisplayName("Should reject assigning inactive lecturer to subject")
    void assignLecturer_inactiveLecturer_throwsException() {
        lecturer.setStatus(UserStatus.PENDING_APPROVAL);
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(userRepository.findById(50L)).thenReturn(Optional.of(lecturer));

        LecturerAssignmentRequest req = new LecturerAssignmentRequest(100L, 50L, "2025/2026", "Semester 1");
        assertThrows(InvalidStatusTransitionException.class, () -> academicService.assignLecturer(req));
    }

    @Test
    @DisplayName("Should reject duplicate active lecturer assignment")
    void assignLecturer_duplicate_throwsException() {
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(userRepository.findById(50L)).thenReturn(Optional.of(lecturer));
        when(lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
                100L, 50L, "2025/2026", "Semester 1")).thenReturn(true);

        LecturerAssignmentRequest req = new LecturerAssignmentRequest(100L, 50L, "2025/2026", "Semester 1");
        assertThrows(DuplicateResourceException.class, () -> academicService.assignLecturer(req));
    }

    // ==========================================
    // STUDENT ENROLMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Should successfully enrol an active student in an appropriate course and subject")
    void enrolStudent_success() {
        when(userRepository.findById(60L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndStatus(
                60L, 100L, "2025/2026", "Semester 1", EnrollmentStatus.ACTIVE)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> {
            Enrollment e = i.getArgument(0);
            e.setId(1L);
            return e;
        });

        EnrollmentRequest req = new EnrollmentRequest(60L, 10L, 100L, "2025/2026", "Semester 1");
        EnrollmentResponse res = academicService.enrolStudent(req);

        assertNotNull(res);
        assertEquals("Bob Smith", res.getStudentName());
        assertEquals("CS101", res.getSubjectCode());
        assertEquals(EnrollmentStatus.ACTIVE, res.getStatus());
    }

    @Test
    @DisplayName("Should reject enrolling non-student user")
    void enrolStudent_nonStudent_throwsException() {
        when(userRepository.findById(50L)).thenReturn(Optional.of(lecturer));

        EnrollmentRequest req = new EnrollmentRequest(50L, 10L, 100L, "2025/2026", "Semester 1");
        assertThrows(InvalidStatusTransitionException.class, () -> academicService.enrolStudent(req));
    }

    @Test
    @DisplayName("Should reject enrolling student when subject does not belong to course")
    void enrolStudent_subjectCourseMismatch_throwsException() {
        Course otherCourse = new Course("BBA", "Business Admin", "Business", dept);
        otherCourse.setId(25L);

        when(userRepository.findById(60L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(25L)).thenReturn(Optional.of(otherCourse));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));

        EnrollmentRequest req = new EnrollmentRequest(60L, 25L, 100L, "2025/2026", "Semester 1");
        assertThrows(InvalidStatusTransitionException.class, () -> academicService.enrolStudent(req));
    }

    @Test
    @DisplayName("Should reject duplicate active student enrolment")
    void enrolStudent_duplicate_throwsException() {
        when(userRepository.findById(60L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(subject));
        when(enrollmentRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndStatus(
                60L, 100L, "2025/2026", "Semester 1", EnrollmentStatus.ACTIVE)).thenReturn(true);

        EnrollmentRequest req = new EnrollmentRequest(60L, 10L, 100L, "2025/2026", "Semester 1");
        assertThrows(DuplicateResourceException.class, () -> academicService.enrolStudent(req));
    }

    // ==========================================
    // PORTAL QUERIES & SECURITY TESTS
    // ==========================================

    @Test
    @DisplayName("Lecturer can view assigned subjects")
    void getLecturerAssignedSubjects_success() {
        LecturerAssignment assignment = new LecturerAssignment(subject, lecturer, "2025/2026", "Semester 1");
        when(lecturerAssignmentRepository.findByLecturerEmailAndActiveTrue("alice@university.edu"))
                .thenReturn(List.of(assignment));

        List<SubjectResponse> subjects = academicService.getLecturerAssignedSubjects("alice@university.edu");
        assertEquals(1, subjects.size());
        assertEquals("CS101", subjects.get(0).getCode());
    }

    @Test
    @DisplayName("Lecturer cannot view students of unassigned subject")
    void getLecturerSubjectStudents_unassigned_throwsAccessDenied() {
        when(lecturerAssignmentRepository.findByLecturerEmailAndActiveTrue("alice@university.edu"))
                .thenReturn(List.of());

        assertThrows(AccessDeniedException.class, () ->
                academicService.getLecturerSubjectStudents(100L, "alice@university.edu")
        );
    }

    @Test
    @DisplayName("Student can view their own enrolments")
    void getStudentEnrolments_success() {
        Enrollment enrollment = new Enrollment(student, course, subject, "2025/2026", "Semester 1");
        when(enrollmentRepository.findByStudentEmail("bob@student.university.edu"))
                .thenReturn(List.of(enrollment));

        List<EnrollmentResponse> enrolments = academicService.getStudentEnrolments("bob@student.university.edu");
        assertEquals(1, enrolments.size());
        assertEquals("CS101", enrolments.get(0).getSubjectCode());
    }
}
