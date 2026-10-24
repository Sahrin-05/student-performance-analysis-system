package com.sahrin.spas.controller;

import com.sahrin.spas.dto.performance.StudentOverallResultResponse;
import com.sahrin.spas.dto.performance.StudentPublishedResultsResponse;
import com.sahrin.spas.service.PerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic/student/performance")
@PreAuthorize("hasRole('STUDENT')")
public class StudentPerformanceController {

    private final PerformanceService performanceService;

    public StudentPerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/enrolments/{enrollmentId}/results")
    public ResponseEntity<StudentPublishedResultsResponse> getMySubjectResults(
            @PathVariable("enrollmentId") Long enrollmentId,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();
        StudentPublishedResultsResponse response = performanceService.getMySubjectResults(enrollmentId, studentEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-results")
    public ResponseEntity<List<StudentOverallResultResponse>> getMyAllResults(
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();
        List<StudentOverallResultResponse> list = performanceService.getMyAllPublishedResults(studentEmail);
        return ResponseEntity.ok(list);
    }
}
