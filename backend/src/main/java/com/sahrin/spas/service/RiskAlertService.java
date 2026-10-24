package com.sahrin.spas.service;

import com.sahrin.spas.dto.risk.AlertActionRequest;
import com.sahrin.spas.dto.risk.AlertResolutionRequest;
import com.sahrin.spas.dto.risk.AlertReviewRequest;
import com.sahrin.spas.dto.risk.AlertSummaryStatsResponse;
import com.sahrin.spas.dto.risk.RiskAlertResponse;
import com.sahrin.spas.dto.risk.StudentRiskAlertResponse;
import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;

import java.util.List;

public interface RiskAlertService {

    // ==========================================
    // EVALUATION ENGINE
    // ==========================================

    List<RiskAlertResponse> evaluateSubjectRisk(Long subjectId, String academicYear, String semester, String initiatedByEmail);

    List<RiskAlertResponse> evaluateAllSubjectsRisk(String academicYear, String semester, String adminEmail);

    // ==========================================
    // ROLE-BASED RETRIEVAL
    // ==========================================

    // Admin retrieval
    List<RiskAlertResponse> getAllAlerts(AlertRiskType riskType, AlertSeverity severity,
                                         AlertStatus status, Long subjectId, Long studentId);

    AlertSummaryStatsResponse getAdminSummaryStats();

    // Lecturer retrieval
    List<RiskAlertResponse> getLecturerAlertsForSubject(Long subjectId, String academicYear, String semester, String lecturerEmail);

    List<RiskAlertResponse> getLecturerAllAlerts(String lecturerEmail);

    AlertSummaryStatsResponse getLecturerSummaryStats(String lecturerEmail);

    // Student retrieval (sanitized view)
    List<StudentRiskAlertResponse> getMyAlerts(String studentEmail);

    RiskAlertResponse getAlertById(Long alertId, String staffEmail);

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    RiskAlertResponse reviewAlert(Long alertId, AlertReviewRequest request, String reviewerEmail);

    RiskAlertResponse recordAction(Long alertId, AlertActionRequest request, String staffEmail);

    RiskAlertResponse resolveAlert(Long alertId, AlertResolutionRequest request, String staffEmail);
}
