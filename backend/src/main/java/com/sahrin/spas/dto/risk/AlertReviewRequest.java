package com.sahrin.spas.dto.risk;

import jakarta.validation.constraints.Size;

public class AlertReviewRequest {

    @Size(max = 1000, message = "Review comment must not exceed 1000 characters")
    private String comment;

    public AlertReviewRequest() {
    }

    public AlertReviewRequest(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
