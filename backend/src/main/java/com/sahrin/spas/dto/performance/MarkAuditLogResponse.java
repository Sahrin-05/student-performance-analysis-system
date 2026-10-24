package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.MarkAuditLog;

import java.time.LocalDateTime;

public class MarkAuditLogResponse {

    private Long id;
    private Long markId;
    private Long assessmentId;
    private Long enrollmentId;
    private String studentIdentifier;
    private Double previousScore;
    private double newScore;
    private String action;
    private String reason;
    private String performedByName;
    private LocalDateTime performedAt;

    public MarkAuditLogResponse() {
    }

    public MarkAuditLogResponse(MarkAuditLog log) {
        this.id = log.getId();
        this.markId = log.getMarkId();
        this.assessmentId = log.getAssessmentId();
        this.enrollmentId = log.getEnrollmentId();
        this.studentIdentifier = log.getStudentIdentifier();
        this.previousScore = log.getPreviousScore();
        this.newScore = log.getNewScore();
        this.action = log.getAction();
        this.reason = log.getReason();
        if (log.getPerformedBy() != null) {
            this.performedByName = log.getPerformedBy().getFullName();
        }
        this.performedAt = log.getPerformedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMarkId() {
        return markId;
    }

    public void setMarkId(Long markId) {
        this.markId = markId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStudentIdentifier() {
        return studentIdentifier;
    }

    public void setStudentIdentifier(String studentIdentifier) {
        this.studentIdentifier = studentIdentifier;
    }

    public Double getPreviousScore() {
        return previousScore;
    }

    public void setPreviousScore(Double previousScore) {
        this.previousScore = previousScore;
    }

    public double getNewScore() {
        return newScore;
    }

    public void setNewScore(double newScore) {
        this.newScore = newScore;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
}
