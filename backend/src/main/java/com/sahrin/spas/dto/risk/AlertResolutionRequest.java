package com.sahrin.spas.dto.risk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AlertResolutionRequest {

    @NotBlank(message = "Resolution notes are required to close an academic alert")
    @Size(max = 2000, message = "Resolution notes must not exceed 2000 characters")
    private String resolutionNotes;

    public AlertResolutionRequest() {
    }

    public AlertResolutionRequest(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
