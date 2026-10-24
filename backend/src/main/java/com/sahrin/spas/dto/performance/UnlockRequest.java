package com.sahrin.spas.dto.performance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UnlockRequest {

    @NotBlank(message = "Unlock reason is required")
    @Size(min = 5, max = 500, message = "Unlock reason must be between 5 and 500 characters")
    private String reason;

    public UnlockRequest() {
    }

    public UnlockRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
