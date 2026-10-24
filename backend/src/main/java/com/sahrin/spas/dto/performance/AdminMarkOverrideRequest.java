package com.sahrin.spas.dto.performance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminMarkOverrideRequest {

    @NotNull(message = "New score is required")
    @DecimalMin(value = "0.00", message = "Score cannot be negative")
    private Double newScore;

    @NotBlank(message = "Administrative override reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    public AdminMarkOverrideRequest() {
    }

    public AdminMarkOverrideRequest(Double newScore, String reason) {
        this.newScore = newScore;
        this.reason = reason;
    }

    public Double getNewScore() {
        return newScore;
    }

    public void setNewScore(Double newScore) {
        this.newScore = newScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
