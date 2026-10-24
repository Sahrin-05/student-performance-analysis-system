package com.sahrin.spas.dto.performance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MarkEntryRequest {

    @NotNull(message = "Assessment ID is required")
    private Long assessmentId;

    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.00", message = "Score cannot be negative")
    private Double score;

    @Size(max = 500, message = "Comments cannot exceed 500 characters")
    private String comments;

    public MarkEntryRequest() {
    }

    public MarkEntryRequest(Long assessmentId, Long enrollmentId, Double score, String comments) {
        this.assessmentId = assessmentId;
        this.enrollmentId = enrollmentId;
        this.score = score;
        this.comments = comments;
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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
