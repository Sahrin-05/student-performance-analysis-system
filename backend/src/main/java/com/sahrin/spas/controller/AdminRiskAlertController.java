package com.sahrin.spas.controller;

import com.sahrin.spas.dto.risk.AlertActionRequest;
import com.sahrin.spas.dto.risk.AlertResolutionRequest;
import com.sahrin.spas.dto.risk.AlertReviewRequest;
import com.sahrin.spas.dto.risk.AlertSummaryStatsResponse;
import com.sahrin.spas.dto.risk.EvaluateRiskRequest;
import com.sahrin.spas.dto.risk.RiskAlertResponse;
import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
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
@RequestMapping("/api/admin/risk-alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRiskAlertController {

    private final RiskAlertService riskAlertService;

    public AdminRiskAlertController(RiskAlertService riskAlertService) {
        this.riskAlertService = riskAlertService;
    }

    @GetMapping
    public ResponseEntity<List<RiskAlertResponse>> getAllAlerts(
            @RequestParam(name = "riskType", required = false) AlertRiskType riskType,
            @RequestParam(name = "severity", required = false) AlertSeverity severity,
            @RequestParam(name = "status", required = false) AlertStatus status,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "studentId", required = false) Long studentId
    ) {
        List<RiskAlertResponse> alerts = riskAlertService.getAllAlerts(riskType, severity, status, subjectId, studentId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/summary")
    public ResponseEntity<AlertSummaryStatsResponse> getAdminSummaryStats() {
        return ResponseEntity.ok(riskAlertService.getAdminSummaryStats());
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<RiskAlertResponse> getAlertById(
            @PathVariable("alertId") Long alertId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(riskAlertService.getAlertById(alertId, authentication.getName()));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<List<RiskAlertResponse>> evaluateRisk(
            @Valid @RequestBody EvaluateRiskRequest request,
            Authentication authentication
    ) {
        List<RiskAlertResponse> results;
        if (request.getSubjectId() != null && request.getSubjectId() > 0) {
            results = riskAlertService.evaluateSubjectRisk(
                    request.getSubjectId(), request.getAcademicYear(), request.getSemester(), authentication.getName());
        } else {
            results = riskAlertService.evaluateAllSubjectsRisk(
                    request.getAcademicYear(), request.getSemester(), authentication.getName());
        }
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
