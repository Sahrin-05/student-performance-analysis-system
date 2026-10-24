package com.sahrin.spas.dto.performance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BulkMarkEntryRequest {

    @NotNull(message = "Assessment ID is required")
    private Long assessmentId;

    @NotEmpty(message = "Marks list cannot be empty")
    @Valid
    private List<StudentMarkItem> items;

    public BulkMarkEntryRequest() {
    }

    public BulkMarkEntryRequest(Long assessmentId, List<StudentMarkItem> items) {
        this.assessmentId = assessmentId;
        this.items = items;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<StudentMarkItem> getItems() {
        return items;
    }

    public void setItems(List<StudentMarkItem> items) {
        this.items = items;
    }

    public static class StudentMarkItem {
        @NotNull(message = "Enrollment ID is required")
        private Long enrollmentId;

        @NotNull(message = "Score is required")
        private Double score;

        private String comments;

        public StudentMarkItem() {
        }

        public StudentMarkItem(Long enrollmentId, Double score, String comments) {
            this.enrollmentId = enrollmentId;
            this.score = score;
            this.comments = comments;
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
}
