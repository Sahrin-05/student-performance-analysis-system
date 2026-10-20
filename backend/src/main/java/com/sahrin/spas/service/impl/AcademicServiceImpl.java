package com.sahrin.spas.service.impl;

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
import com.sahrin.spas.exception.DuplicateResourceException;
import com.sahrin.spas.exception.InvalidStatusTransitionException;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.CourseRepository;
import com.sahrin.spas.repository.DepartmentRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AcademicService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AcademicServiceImpl implements AcademicService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final LecturerAssignmentRepository lecturerAssignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public AcademicServiceImpl(
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            SubjectRepository subjectRepository,
            LecturerAssignmentRepository lecturerAssignmentRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
        this.lecturerAssignmentRepository = lecturerAssignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // DEPARTMENT OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (departmentRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Department with code '" + code + "' already exists.");
        }
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department with name '" + name + "' already exists.");
        }

        Department department = new Department(
                code,
                name,
                request.getDescription() != null ? request.getDescription().trim() : null
        );
        Department saved = departmentRepository.save(department);
        return mapToDepartmentResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("Department with code '" + code + "' already exists.");
        }
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Department with name '" + name + "' already exists.");
        }

        department.setCode(code);
        department.setName(name);
        department.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Department updated = departmentRepository.save(department);
        return mapToDepartmentResponse(updated);
    }

    @Override
    @Transactional
    public DepartmentResponse setDepartmentStatus(Long id, boolean active) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        if (!active && courseRepository.existsByDepartmentIdAndActiveTrue(id)) {
            throw new InvalidStatusTransitionException(
                    "Cannot deactivate department '" + department.getName() + "' while it has active courses. Deactivate associated courses first."
            );
        }

        department.setActive(active);
        Department saved = departmentRepository.save(department);
        return mapToDepartmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments(Boolean activeOnly) {
        List<Department> departments = Boolean.TRUE.equals(activeOnly)
                ? departmentRepository.findByActiveTrueOrderByNameAsc()
                : departmentRepository.findAllByOrderByNameAsc();
        return departments.stream().map(this::mapToDepartmentResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return mapToDepartmentResponse(department);
    }

    // ==========================================
    // COURSE OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (courseRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Course with code '" + code + "' already exists.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        if (!department.isActive()) {
            throw new InvalidStatusTransitionException("Cannot associate course with an inactive department.");
        }

        Course course = new Course(
                code,
                name,
                request.getDescription() != null ? request.getDescription().trim() : null,
                department
        );
        Course saved = courseRepository.save(course);
        return mapToCourseResponse(saved);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (courseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("Course with code '" + code + "' already exists.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        if (!department.isActive()) {
            throw new InvalidStatusTransitionException("Cannot associate course with an inactive department.");
        }

        course.setCode(code);
        course.setName(name);
        course.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        course.setDepartment(department);

        Course updated = courseRepository.save(course);
        return mapToCourseResponse(updated);
    }

    @Override
    @Transactional
    public CourseResponse setCourseStatus(Long id, boolean active) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        if (!active && subjectRepository.existsByCourseId(id)) {
            boolean hasActiveSubjects = subjectRepository.findByCourseIdAndActiveTrueOrderByNameAsc(id).size() > 0;
            if (hasActiveSubjects) {
                throw new InvalidStatusTransitionException(
                        "Cannot deactivate course '" + course.getName() + "' while it has active subjects. Deactivate associated subjects first."
                );
            }
        }

        course.setActive(active);
        Course saved = courseRepository.save(course);
        return mapToCourseResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses(Long departmentId, Boolean activeOnly) {
        List<Course> courses;
        if (departmentId != null) {
            courses = Boolean.TRUE.equals(activeOnly)
                    ? courseRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(departmentId)
                    : courseRepository.findByDepartmentIdOrderByNameAsc(departmentId);
        } else {
            courses = Boolean.TRUE.equals(activeOnly)
                    ? courseRepository.findByActiveTrueOrderByNameAsc()
                    : courseRepository.findAllByOrderByNameAsc();
        }
        return courses.stream().map(this::mapToCourseResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        return mapToCourseResponse(course);
    }

    // ==========================================
    // SUBJECT OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (subjectRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Subject with code '" + code + "' already exists.");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (!course.isActive()) {
            throw new InvalidStatusTransitionException("Cannot associate subject with an inactive course.");
        }

        Department department = course.getDepartment();
        if (!department.isActive()) {
            throw new InvalidStatusTransitionException("Cannot associate subject with an inactive department.");
        }

        Subject subject = new Subject(
                code,
                name,
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getCredits(),
                request.getSemester(),
                course,
                department
        );
        Subject saved = subjectRepository.save(subject);
        return mapToSubjectResponse(saved);
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + id));

        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (subjectRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("Subject with code '" + code + "' already exists.");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (!course.isActive()) {
            throw new InvalidStatusTransitionException("Cannot associate subject with an inactive course.");
        }

        subject.setCode(code);
        subject.setName(name);
        subject.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        subject.setCredits(request.getCredits());
        subject.setSemester(request.getSemester());
        subject.setCourse(course);
        subject.setDepartment(course.getDepartment());

        Subject updated = subjectRepository.save(subject);
        return mapToSubjectResponse(updated);
    }

    @Override
    @Transactional
    public SubjectResponse setSubjectStatus(Long id, boolean active) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + id));

        subject.setActive(active);
        Subject saved = subjectRepository.save(subject);
        return mapToSubjectResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects(Long courseId, Long departmentId, Boolean activeOnly) {
        List<Subject> subjects;
        if (courseId != null) {
            subjects = Boolean.TRUE.equals(activeOnly)
                    ? subjectRepository.findByCourseIdAndActiveTrueOrderByNameAsc(courseId)
                    : subjectRepository.findByCourseIdOrderByNameAsc(courseId);
        } else if (departmentId != null) {
            subjects = subjectRepository.findByDepartmentIdOrderByNameAsc(departmentId);
            if (Boolean.TRUE.equals(activeOnly)) {
                subjects = subjects.stream().filter(Subject::isActive).toList();
            }
        } else {
            subjects = Boolean.TRUE.equals(activeOnly)
                    ? subjectRepository.findByActiveTrueOrderByNameAsc()
                    : subjectRepository.findAllByOrderByNameAsc();
        }
        return subjects.stream().map(this::mapToSubjectResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + id));
        return mapToSubjectResponse(subject);
    }

    // ==========================================
    // LECTURER ASSIGNMENT OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public LecturerAssignmentResponse assignLecturer(LecturerAssignmentRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + request.getSubjectId()));

        if (!subject.isActive()) {
            throw new InvalidStatusTransitionException("Cannot assign lecturer to an inactive subject.");
        }

        User lecturer = userRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getLecturerId()));

        if (lecturer.getRole() != Role.LECTURER) {
            throw new InvalidStatusTransitionException("Only users with role LECTURER can be assigned to subjects.");
        }

        if (lecturer.getStatus() != User.UserStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only ACTIVE lecturers can receive subject assignments.");
        }

        String academicYear = request.getAcademicYear().trim();
        String semester = request.getSemester().trim();

        if (lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
                subject.getId(), lecturer.getId(), academicYear, semester)) {
            throw new DuplicateResourceException("This lecturer is already actively assigned to this subject for " + academicYear + " " + semester + ".");
        }

        LecturerAssignment assignment = new LecturerAssignment(
                subject,
                lecturer,
                academicYear,
                semester
        );
        LecturerAssignment saved = lecturerAssignmentRepository.save(assignment);
        return mapToAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public LecturerAssignmentResponse updateAssignment(Long id, LecturerAssignmentRequest request) {
        LecturerAssignment assignment = lecturerAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + id));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + request.getSubjectId()));

        User lecturer = userRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getLecturerId()));

        if (lecturer.getRole() != Role.LECTURER) {
            throw new InvalidStatusTransitionException("Only users with role LECTURER can be assigned to subjects.");
        }

        if (lecturer.getStatus() != User.UserStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only ACTIVE lecturers can receive subject assignments.");
        }

        assignment.setSubject(subject);
        assignment.setLecturer(lecturer);
        assignment.setAcademicYear(request.getAcademicYear().trim());
        assignment.setSemester(request.getSemester().trim());

        LecturerAssignment saved = lecturerAssignmentRepository.save(assignment);
        return mapToAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public LecturerAssignmentResponse setAssignmentStatus(Long id, boolean active) {
        LecturerAssignment assignment = lecturerAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + id));

        assignment.setActive(active);
        LecturerAssignment saved = lecturerAssignmentRepository.save(assignment);
        return mapToAssignmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LecturerAssignmentResponse> getAllAssignments(Long subjectId, Long lecturerId) {
        List<LecturerAssignment> assignments = lecturerAssignmentRepository.findAllByOrderByAssignedAtDesc();

        if (subjectId != null) {
            assignments = assignments.stream().filter(a -> a.getSubject().getId().equals(subjectId)).toList();
        }
        if (lecturerId != null) {
            assignments = assignments.stream().filter(a -> a.getLecturer().getId().equals(lecturerId)).toList();
        }

        return assignments.stream().map(this::mapToAssignmentResponse).toList();
    }

    // ==========================================
    // STUDENT ENROLMENT OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public EnrollmentResponse enrolStudent(EnrollmentRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        if (student.getRole() != Role.STUDENT) {
            throw new InvalidStatusTransitionException("Only users with role STUDENT can be enrolled in subjects.");
        }

        if (student.getStatus() != User.UserStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only ACTIVE students can be enrolled.");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (!course.isActive()) {
            throw new InvalidStatusTransitionException("Cannot enrol student in an inactive course.");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + request.getSubjectId()));

        if (!subject.isActive()) {
            throw new InvalidStatusTransitionException("Cannot enrol student in an inactive subject.");
        }

        if (!subject.getCourse().getId().equals(course.getId())) {
            throw new InvalidStatusTransitionException("Subject '" + subject.getName() + "' does not belong to course '" + course.getName() + "'.");
        }

        String academicYear = request.getAcademicYear().trim();
        String semester = request.getSemester().trim();

        if (enrollmentRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndStatus(
                student.getId(), subject.getId(), academicYear, semester, EnrollmentStatus.ACTIVE)) {
            throw new DuplicateResourceException("Student is already actively enrolled in this subject for " + academicYear + " " + semester + ".");
        }

        Enrollment enrollment = new Enrollment(
                student,
                course,
                subject,
                academicYear,
                semester
        );
        Enrollment saved = enrollmentRepository.save(enrollment);
        return mapToEnrollmentResponse(saved);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateEnrollmentStatus(Long id, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrolment not found with ID: " + id));

        enrollment.setStatus(status);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return mapToEnrollmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments(Long courseId, Long subjectId, Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findAllByOrderByEnrolledAtDesc();

        if (courseId != null) {
            enrollments = enrollments.stream().filter(e -> e.getCourse().getId().equals(courseId)).toList();
        }
        if (subjectId != null) {
            enrollments = enrollments.stream().filter(e -> e.getSubject().getId().equals(subjectId)).toList();
        }
        if (studentId != null) {
            enrollments = enrollments.stream().filter(e -> e.getStudent().getId().equals(studentId)).toList();
        }

        return enrollments.stream().map(this::mapToEnrollmentResponse).toList();
    }

    // ==========================================
    // LECTURER PORTAL OPERATIONS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getLecturerAssignedSubjects(String lecturerEmail) {
        List<LecturerAssignment> assignments = lecturerAssignmentRepository.findByLecturerEmailAndActiveTrue(lecturerEmail);
        return assignments.stream()
                .map(LecturerAssignment::getSubject)
                .filter(Subject::isActive)
                .distinct()
                .map(this::mapToSubjectResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getLecturerSubjectStudents(Long subjectId, String lecturerEmail) {
        List<LecturerAssignment> assignments = lecturerAssignmentRepository.findByLecturerEmailAndActiveTrue(lecturerEmail);
        boolean isAssigned = assignments.stream().anyMatch(a -> a.getSubject().getId().equals(subjectId));

        if (!isAssigned) {
            throw new AccessDeniedException("Access denied: You are not actively assigned to teach this subject.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);
        return enrollments.stream().map(this::mapToEnrollmentResponse).toList();
    }

    // ==========================================
    // STUDENT PORTAL OPERATIONS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrolments(String studentEmail) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentEmail(studentEmail);
        return enrollments.stream().map(this::mapToEnrollmentResponse).toList();
    }

    // ==========================================
    // DTO MAPPERS
    // ==========================================

    private DepartmentResponse mapToDepartmentResponse(Department d) {
        return new DepartmentResponse(
                d.getId(),
                d.getCode(),
                d.getName(),
                d.getDescription(),
                d.isActive(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }

    private CourseResponse mapToCourseResponse(Course c) {
        return new CourseResponse(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getDescription(),
                c.getDepartment().getId(),
                c.getDepartment().getCode(),
                c.getDepartment().getName(),
                c.isActive(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private SubjectResponse mapToSubjectResponse(Subject s) {
        return new SubjectResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getDescription(),
                s.getCredits(),
                s.getSemester(),
                s.getCourse().getId(),
                s.getCourse().getCode(),
                s.getCourse().getName(),
                s.getDepartment().getId(),
                s.getDepartment().getCode(),
                s.getDepartment().getName(),
                s.isActive(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    private LecturerAssignmentResponse mapToAssignmentResponse(LecturerAssignment a) {
        return new LecturerAssignmentResponse(
                a.getId(),
                a.getSubject().getId(),
                a.getSubject().getCode(),
                a.getSubject().getName(),
                a.getSubject().getCourse().getName(),
                a.getLecturer().getId(),
                a.getLecturer().getFullName(),
                a.getLecturer().getEmail(),
                a.getLecturer().getIdentifier(),
                a.getAcademicYear(),
                a.getSemester(),
                a.isActive(),
                a.getAssignedAt()
        );
    }

    private EnrollmentResponse mapToEnrollmentResponse(Enrollment e) {
        return new EnrollmentResponse(
                e.getId(),
                e.getStudent().getId(),
                e.getStudent().getFullName(),
                e.getStudent().getEmail(),
                e.getStudent().getIdentifier(),
                e.getCourse().getId(),
                e.getCourse().getCode(),
                e.getCourse().getName(),
                e.getSubject().getId(),
                e.getSubject().getCode(),
                e.getSubject().getName(),
                e.getSubject().getCredits(),
                e.getAcademicYear(),
                e.getSemester(),
                e.getStatus(),
                e.getEnrolledAt()
        );
    }
}
