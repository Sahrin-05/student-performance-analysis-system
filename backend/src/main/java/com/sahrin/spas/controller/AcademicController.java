package com.sahrin.spas.controller;

import com.sahrin.spas.dto.academic.EnrollmentResponse;
import com.sahrin.spas.dto.academic.SubjectResponse;
import com.sahrin.spas.service.AcademicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    // ==========================================
    // LECTURER PORTAL ENDPOINTS
    // ==========================================

    @GetMapping("/lecturer/my-subjects")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<SubjectResponse>> getMyAssignedSubjects(Authentication authentication) {
        String lecturerEmail = authentication.getName();
        List<SubjectResponse> subjects = academicService.getLecturerAssignedSubjects(lecturerEmail);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/lecturer/subjects/{subjectId}/students")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<EnrollmentResponse>> getMySubjectStudents(
            @PathVariable("subjectId") Long subjectId,
            Authentication authentication
    ) {
        String lecturerEmail = authentication.getName();
        List<EnrollmentResponse> students = academicService.getLecturerSubjectStudents(subjectId, lecturerEmail);
        return ResponseEntity.ok(students);
    }

    // ==========================================
    // STUDENT PORTAL ENDPOINTS
    // ==========================================

    @GetMapping("/student/my-enrolments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrolments(Authentication authentication) {
        String studentEmail = authentication.getName();
        List<EnrollmentResponse> enrolments = academicService.getStudentEnrolments(studentEmail);
        return ResponseEntity.ok(enrolments);
    }
}
