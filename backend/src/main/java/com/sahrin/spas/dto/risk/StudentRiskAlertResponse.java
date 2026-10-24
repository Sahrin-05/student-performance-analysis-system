package com.sahrin.spas.dto.risk;

import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.RiskAlert;

import java.time.LocalDateTime;

/**
 * Student-facing view of academic risk alerts.
 * Strictly excludes internal faculty notes, reviewer identities, and administrative commentary.
 */
public class StudentRiskAlertResponse {

    private Long id;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String academicYear;
    private String semester;
    private AlertRiskType riskType;
    private AlertSeverity severity;
    private String reason;
    private Double metricValue;
    private String metricDetails;
    private AlertStatus status;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String advisoryGuidance;

    public StudentRiskAlertResponse() {
    }

    public StudentRiskAlertResponse(RiskAlert alert) {
        if (alert == null) {
            return;
        }
        this.id = alert.getId();
        if (alert.getSubject() != null) {
            this.subjectId = alert.getSubject().getId();
            this.subjectCode = alert.getSubject().getCode();
            this.subjectName = alert.getSubject().getName();
        }
        this.academicYear = alert.getAcademicYear();
        this.semester = alert.getSemester();
        this.riskType = alert.getRiskType();
        this.severity = alert.getSeverity();
        this.reason = alert.getReason();
        this.metricValue = alert.getMetricValue();
        this.metricDetails = alert.getMetricDetails();
        this.status = alert.getStatus();
        this.detectedAt = alert.getDetectedAt();
        this.resolvedAt = alert.getResolvedAt();
        this.advisoryGuidance = generateAdvisoryGuidance(alert.getRiskType(), alert.getSeverity());
    }

    private String generateAdvisoryGuidance(AlertRiskType riskType, AlertSeverity severity) {
        if (riskType == null) {
            return "Please consult with your subject lecturer or academic advisor.";
        }
        return switch (riskType) {
            case ATTENDANCE_WARNING ->
                "Your attendance is currently below the 80% threshold. Please attend upcoming lectures regularly, catch up with your lecturer on missed topics, and ensure valid medical or official leave documents are submitted on time.";
            case LOW_ACADEMIC_MARKS ->
                "Your cumulative score is currently below the passing threshold. We recommend booking office hours with your lecturer, reviewing grading feedback, and joining peer study groups.";
            case PROGRESS_DECLINE ->
                "A significant decline in recent assessment scores was detected. Review assessment rubrics carefully and reach out to your instructor or tutor for targeted guidance.";
            case MISSING_ASSESSMENT ->
                "One or more assessments have not been recorded or submitted. Please check your submission status with your lecturer immediately to avoid academic penalties.";
        };
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public AlertRiskType getRiskType() {
        return riskType;
    }

    public void setRiskType(AlertRiskType riskType) {
        this.riskType = riskType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public String getMetricDetails() {
        return metricDetails;
    }

    public void setMetricDetails(String metricDetails) {
        this.metricDetails = metricDetails;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAdvisoryGuidance() {
        return advisoryGuidance;
    }

    public void setAdvisoryGuidance(String advisoryGuidance) {
        this.advisoryGuidance = advisoryGuidance;
    }
}
