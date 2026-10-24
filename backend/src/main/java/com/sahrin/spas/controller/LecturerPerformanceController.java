package com.sahrin.spas.controller;

import com.sahrin.spas.dto.performance.AssessmentRequest;
import com.sahrin.spas.dto.performance.AssessmentResponse;
import com.sahrin.spas.dto.performance.BulkMarkEntryRequest;
import com.sahrin.spas.dto.performance.MarkEntryRequest;
import com.sahrin.spas.dto.performance.PublishResultsRequest;
import com.sahrin.spas.dto.performance.StudentMarkResponse;
import com.sahrin.spas.dto.performance.SubjectResultPublicationResponse;
import com.sahrin.spas.dto.performance.SubjectRosterMarksResponse;
import com.sahrin.spas.service.PerformanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic/lecturer/performance")
@PreAuthorize("hasRole('LECTURER')")
public class LecturerPerformanceController {

    private final PerformanceService performanceService;

    public LecturerPerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    // ==========================================
    // ASSESSMENTS
    // ==========================================

    @PostMapping("/assessments")
    public ResponseEntity<AssessmentResponse> createAssessment(
            @Valid @RequestBody AssessmentRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AssessmentResponse response = performanceService.createAssessment(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/assessments/{id}")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssessmentRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AssessmentResponse response = performanceService.updateAssessment(id, request, email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/assessments/{id}")
    public ResponseEntity<Void> deleteAssessment(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        performanceService.deleteAssessment(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subjects/{subjectId}/assessments")
    public ResponseEntity<List<AssessmentResponse>> getSubjectAssessments(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<AssessmentResponse> list = performanceService.getSubjectAssessments(subjectId, academicYear, semester, email);
        return ResponseEntity.ok(list);
    }

    // ==========================================
    // MARKS ENTRY & ROSTER
    // ==========================================

    @PostMapping("/marks")
    public ResponseEntity<StudentMarkResponse> enterMark(
            @Valid @RequestBody MarkEntryRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        StudentMarkResponse response = performanceService.enterOrUpdateMark(request, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/marks/bulk")
    public ResponseEntity<List<StudentMarkResponse>> bulkEnterMarks(
            @Valid @RequestBody BulkMarkEntryRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<StudentMarkResponse> responses = performanceService.bulkEnterMarks(request, email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/subjects/{subjectId}/roster-marks")
    public ResponseEntity<SubjectRosterMarksResponse> getSubjectMarksRoster(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SubjectRosterMarksResponse response = performanceService.getSubjectMarksRoster(subjectId, academicYear, semester, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/publish-results")
    public ResponseEntity<SubjectResultPublicationResponse> publishResults(
            @Valid @RequestBody PublishResultsRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SubjectResultPublicationResponse response = performanceService.publishSubjectResults(request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subjects/{subjectId}/publication-status")
    public ResponseEntity<SubjectResultPublicationResponse> getPublicationStatus(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester
    ) {
        SubjectResultPublicationResponse response = performanceService.getPublicationStatus(subjectId, academicYear, semester);
        return ResponseEntity.ok(response);
    }
}
