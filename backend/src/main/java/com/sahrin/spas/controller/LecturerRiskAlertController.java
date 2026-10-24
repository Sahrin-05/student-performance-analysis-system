package com.sahrin.spas.controller;

import com.sahrin.spas.dto.risk.AlertActionRequest;
import com.sahrin.spas.dto.risk.AlertResolutionRequest;
import com.sahrin.spas.dto.risk.AlertReviewRequest;
import com.sahrin.spas.dto.risk.AlertSummaryStatsResponse;
import com.sahrin.spas.dto.risk.EvaluateRiskRequest;
import com.sahrin.spas.dto.risk.RiskAlertResponse;
import com.sahrin.spas.service.RiskAlertService;
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
@RequestMapping("/api/academic/lecturer/risk-alerts")
@PreAuthorize("hasRole('LECTURER')")
public class LecturerRiskAlertController {

    private final RiskAlertService riskAlertService;

    public LecturerRiskAlertController(RiskAlertService riskAlertService) {
        this.riskAlertService = riskAlertService;
    }

    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<List<RiskAlertResponse>> getLecturerAlertsForSubject(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester,
            Authentication authentication
    ) {
        List<RiskAlertResponse> alerts = riskAlertService.getLecturerAlertsForSubject(
                subjectId, academicYear, semester, authentication.getName());
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/my-subjects")
    public ResponseEntity<List<RiskAlertResponse>> getLecturerAllAlerts(Authentication authentication) {
        List<RiskAlertResponse> alerts = riskAlertService.getLecturerAllAlerts(authentication.getName());
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/summary")
    public ResponseEntity<AlertSummaryStatsResponse> getLecturerSummaryStats(Authentication authentication) {
        return ResponseEntity.ok(riskAlertService.getLecturerSummaryStats(authentication.getName()));
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<RiskAlertResponse> getAlertById(
            @PathVariable("alertId") Long alertId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(riskAlertService.getAlertById(alertId, authentication.getName()));
    }

    @PostMapping("/subjects/{subjectId}/evaluate")
    public ResponseEntity<List<RiskAlertResponse>> evaluateSubjectRisk(
            @PathVariable("subjectId") Long subjectId,
            @Valid @RequestBody EvaluateRiskRequest request,
            Authentication authentication
    ) {
        List<RiskAlertResponse> results = riskAlertService.evaluateSubjectRisk(
                subjectId, request.getAcademicYear(), request.getSemester(), authentication.getName());
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/{alertId}/review")
    public ResponseEntity<RiskAlertResponse> reviewAlert(
            @PathVariable("alertId") Long alertId,
            @RequestBody(required = false) AlertReviewRequest request,
            Authentication authentication
    ) {
        AlertReviewRequest req = request != null ? request : new AlertReviewRequest();
        return ResponseEntity.ok(riskAlertService.reviewAlert(alertId, req, authentication.getName()));
    }

    @PatchMapping("/{alertId}/action")
    public ResponseEntity<RiskAlertResponse> recordAction(
            @PathVariable("alertId") Long alertId,
            @Valid @RequestBody AlertActionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(riskAlertService.recordAction(alertId, request, authentication.getName()));
    }

    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<RiskAlertResponse> resolveAlert(
            @PathVariable("alertId") Long alertId,
            @Valid @RequestBody AlertResolutionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(riskAlertService.resolveAlert(alertId, request, authentication.getName()));
    }
}
