package com.sahrin.spas.controller;

import com.sahrin.spas.dto.performance.AdminMarkOverrideRequest;
import com.sahrin.spas.dto.performance.MarkAuditLogResponse;
import com.sahrin.spas.dto.performance.StudentMarkResponse;
import com.sahrin.spas.dto.performance.SubjectResultPublicationResponse;
import com.sahrin.spas.dto.performance.UnlockRequest;
import com.sahrin.spas.service.PerformanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/performance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPerformanceController {

    private final PerformanceService performanceService;

    public AdminPerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping("/subjects/{subjectId}/unlock")
    public ResponseEntity<SubjectResultPublicationResponse> unlockResults(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester,
            @Valid @RequestBody UnlockRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        SubjectResultPublicationResponse response = performanceService.unlockSubjectResults(
                subjectId, academicYear, semester, request, adminEmail);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/marks/{markId}/override")
    public ResponseEntity<StudentMarkResponse> overrideMark(
            @PathVariable("markId") Long markId,
            @Valid @RequestBody AdminMarkOverrideRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        StudentMarkResponse response = performanceService.adminOverrideMark(markId, request, adminEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/marks/{markId}/audit-logs")
    public ResponseEntity<List<MarkAuditLogResponse>> getAuditLogs(
            @PathVariable("markId") Long markId
    ) {
        List<MarkAuditLogResponse> logs = performanceService.getAuditLogsForMark(markId);
        return ResponseEntity.ok(logs);
    }
}
