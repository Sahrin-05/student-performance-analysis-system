package com.sahrin.spas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "mark_audit_logs")
public class MarkAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mark_id", nullable = false)
    private Long markId;

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "student_identifier", nullable = false, length = 50)
    private String studentIdentifier;

    @Column(name = "previous_score")
    private Double previousScore;

    @Column(name = "new_score", nullable = false)
    private double newScore;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private User performedBy;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    public MarkAuditLog() {
    }

    public MarkAuditLog(Long markId, Long assessmentId, Long enrollmentId, String studentIdentifier,
                        Double previousScore, double newScore, String action, String reason, User performedBy) {
        this.markId = markId;
        this.assessmentId = assessmentId;
        this.enrollmentId = enrollmentId;
        this.studentIdentifier = studentIdentifier;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.action = action;
        this.reason = reason;
        this.performedBy = performedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.performedAt = LocalDateTime.now();
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

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
}
