package com.sahrin.spas.controller;

import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.dto.academic.CourseRequest;
import com.sahrin.spas.dto.academic.CourseResponse;
import com.sahrin.spas.dto.academic.DepartmentRequest;
import com.sahrin.spas.dto.academic.DepartmentResponse;
import com.sahrin.spas.dto.academic.EnrollmentRequest;
import com.sahrin.spas.dto.academic.EnrollmentResponse;
import com.sahrin.spas.dto.academic.LecturerAssignmentRequest;
import com.sahrin.spas.dto.academic.LecturerAssignmentResponse;
import com.sahrin.spas.dto.academic.StatusUpdateRequest;
import com.sahrin.spas.dto.academic.SubjectRequest;
import com.sahrin.spas.dto.academic.SubjectResponse;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AcademicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academic")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAcademicController {

    private final AcademicService academicService;
    private final UserRepository userRepository;

    public AdminAcademicController(AcademicService academicService, UserRepository userRepository) {
        this.academicService = academicService;
        this.userRepository = userRepository;
    }

    // ==========================================
    // DEPARTMENTS
    // ==========================================

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponse>> getDepartments(
            @RequestParam(required = false) Boolean activeOnly
    ) {
        List<DepartmentResponse> list = academicService.getAllDepartments(activeOnly);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request
    ) {
        DepartmentResponse created = academicService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable("id") Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        DepartmentResponse updated = academicService.updateDepartment(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/departments/{id}/status")
    public ResponseEntity<DepartmentResponse> setDepartmentStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        boolean active = request.getActive() != null ? request.getActive() : true;
        DepartmentResponse response = academicService.setDepartmentStatus(id, active);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // COURSES
    // ==========================================

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getCourses(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        List<CourseResponse> list = academicService.getAllCourses(departmentId, activeOnly);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request
    ) {
        CourseResponse created = academicService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourseRequest request
    ) {
        CourseResponse updated = academicService.updateCourse(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/courses/{id}/status")
    public ResponseEntity<CourseResponse> setCourseStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        boolean active = request.getActive() != null ? request.getActive() : true;
        CourseResponse response = academicService.setCourseStatus(id, active);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // SUBJECTS
    // ==========================================

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponse>> getSubjects(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        List<SubjectResponse> list = academicService.getAllSubjects(courseId, departmentId, activeOnly);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody SubjectRequest request
    ) {
        SubjectResponse created = academicService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable("id") Long id,
            @Valid @RequestBody SubjectRequest request
    ) {
        SubjectResponse updated = academicService.updateSubject(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/subjects/{id}/status")
    public ResponseEntity<SubjectResponse> setSubjectStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        boolean active = request.getActive() != null ? request.getActive() : true;
        SubjectResponse response = academicService.setSubjectStatus(id, active);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // LECTURER ASSIGNMENTS
    // ==========================================

    @GetMapping("/lecturer-assignments")
    public ResponseEntity<List<LecturerAssignmentResponse>> getAssignments(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long lecturerId
    ) {
        List<LecturerAssignmentResponse> list = academicService.getAllAssignments(subjectId, lecturerId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/lecturer-assignments")
    public ResponseEntity<LecturerAssignmentResponse> assignLecturer(
            @Valid @RequestBody LecturerAssignmentRequest request
    ) {
        LecturerAssignmentResponse created = academicService.assignLecturer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/lecturer-assignments/{id}")
    public ResponseEntity<LecturerAssignmentResponse> updateAssignment(
            @PathVariable("id") Long id,
            @Valid @RequestBody LecturerAssignmentRequest request
    ) {
        LecturerAssignmentResponse updated = academicService.updateAssignment(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/lecturer-assignments/{id}/status")
    public ResponseEntity<LecturerAssignmentResponse> setAssignmentStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        boolean active = request.getActive() != null ? request.getActive() : true;
        LecturerAssignmentResponse response = academicService.setAssignmentStatus(id, active);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // STUDENT ENROLMENTS
    // ==========================================

    @GetMapping("/enrolments")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollments(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long studentId
    ) {
        List<EnrollmentResponse> list = academicService.getAllEnrollments(courseId, subjectId, studentId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/enrolments")
    public ResponseEntity<EnrollmentResponse> enrolStudent(
            @Valid @RequestBody EnrollmentRequest request
    ) {
        EnrollmentResponse created = academicService.enrolStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/enrolments/{id}/status")
    public ResponseEntity<EnrollmentResponse> updateEnrollmentStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        EnrollmentStatus status = EnrollmentStatus.valueOf(request.getStatus().trim().toUpperCase());
        EnrollmentResponse response = academicService.updateEnrollmentStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // USER LOOKUPS FOR SELECTION DROPDOWNS
    // ==========================================

    @GetMapping("/active-lecturers")
    public ResponseEntity<List<UserResponse>> getActiveLecturers() {
        List<User> lecturers = userRepository.findByRoleAndStatusOrderByFullNameAsc(
                Role.LECTURER, User.UserStatus.ACTIVE
        );
        List<UserResponse> responses = lecturers.stream()
                .map(u -> new UserResponse(u.getId(), u.getFullName(), u.getIdentifier(), u.getEmail(), u.getRole(), u.getStatus()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active-students")
    public ResponseEntity<List<UserResponse>> getActiveStudents() {
        List<User> students = userRepository.findByRoleAndStatusOrderByFullNameAsc(
                Role.STUDENT, User.UserStatus.ACTIVE
        );
        List<UserResponse> responses = students.stream()
                .map(u -> new UserResponse(u.getId(), u.getFullName(), u.getIdentifier(), u.getEmail(), u.getRole(), u.getStatus()))
                .toList();
        return ResponseEntity.ok(responses);
    }
}
