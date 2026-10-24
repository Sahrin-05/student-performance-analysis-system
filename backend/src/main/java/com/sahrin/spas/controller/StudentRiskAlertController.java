package com.sahrin.spas.controller;

import com.sahrin.spas.dto.risk.StudentRiskAlertResponse;
import com.sahrin.spas.service.RiskAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic/student/risk-alerts")
@PreAuthorize("hasRole('STUDENT')")
public class StudentRiskAlertController {

    private final RiskAlertService riskAlertService;

    public StudentRiskAlertController(RiskAlertService riskAlertService) {
        this.riskAlertService = riskAlertService;
    }

    @GetMapping("/my-alerts")
    public ResponseEntity<List<StudentRiskAlertResponse>> getMyAlerts(Authentication authentication) {
        List<StudentRiskAlertResponse> alerts = riskAlertService.getMyAlerts(authentication.getName());
        return ResponseEntity.ok(alerts);
    }
}
