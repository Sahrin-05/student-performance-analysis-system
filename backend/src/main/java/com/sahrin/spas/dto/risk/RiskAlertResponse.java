package com.sahrin.spas.dto.risk;

import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.RiskAlert;

import java.time.LocalDateTime;

public class RiskAlertResponse {

    private Long id;
    private Long studentId;
    private String studentIdentifier;
    private String studentName;
    private String studentEmail;

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

    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;

    private String actionNotes;
    private Long actionRecordedById;
    private String actionRecordedByName;
    private LocalDateTime actionRecordedAt;

    private Long resolvedById;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;

    private LocalDateTime createdAt;

    public RiskAlertResponse() {
    }

    public RiskAlertResponse(RiskAlert alert) {
        if (alert == null) {
            return;
        }
        this.id = alert.getId();
        if (alert.getStudent() != null) {
            this.studentId = alert.getStudent().getId();
            this.studentIdentifier = alert.getStudent().getIdentifier();
            this.studentName = alert.getStudent().getFullName();
            this.studentEmail = alert.getStudent().getEmail();
        }
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

        if (alert.getReviewedBy() != null) {
            this.reviewedById = alert.getReviewedBy().getId();
            this.reviewedByName = alert.getReviewedBy().getFullName();
        }
        this.reviewedAt = alert.getReviewedAt();

        this.actionNotes = alert.getActionNotes();
        if (alert.getActionRecordedBy() != null) {
            this.actionRecordedById = alert.getActionRecordedBy().getId();
            this.actionRecordedByName = alert.getActionRecordedBy().getFullName();
        }
        this.actionRecordedAt = alert.getActionRecordedAt();

        if (alert.getResolvedBy() != null) {
            this.resolvedById = alert.getResolvedBy().getId();
            this.resolvedByName = alert.getResolvedBy().getFullName();
        }
        this.resolvedAt = alert.getResolvedAt();
        this.resolutionNotes = alert.getResolutionNotes();
        this.createdAt = alert.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentIdentifier() {
        return studentIdentifier;
    }

    public void setStudentIdentifier(String studentIdentifier) {
        this.studentIdentifier = studentIdentifier;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
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

    public Long getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Long reviewedById) {
        this.reviewedById = reviewedById;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getActionNotes() {
        return actionNotes;
    }

    public void setActionNotes(String actionNotes) {
        this.actionNotes = actionNotes;
    }

    public Long getActionRecordedById() {
        return actionRecordedById;
    }

    public void setActionRecordedById(Long actionRecordedById) {
        this.actionRecordedById = actionRecordedById;
    }

    public String getActionRecordedByName() {
        return actionRecordedByName;
    }

    public void setActionRecordedByName(String actionRecordedByName) {
        this.actionRecordedByName = actionRecordedByName;
    }

    public LocalDateTime getActionRecordedAt() {
        return actionRecordedAt;
    }

    public void setActionRecordedAt(LocalDateTime actionRecordedAt) {
        this.actionRecordedAt = actionRecordedAt;
    }

    public Long getResolvedById() {
        return resolvedById;
    }

    public void setResolvedById(Long resolvedById) {
        this.resolvedById = resolvedById;
    }

    public String getResolvedByName() {
        return resolvedByName;
    }

    public void setResolvedByName(String resolvedByName) {
        this.resolvedByName = resolvedByName;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
