package com.sahrin.spas.dto.risk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AlertActionRequest {

    @NotBlank(message = "Action notes are required to document staff intervention")
    @Size(max = 2000, message = "Action notes must not exceed 2000 characters")
    private String actionNotes;

    public AlertActionRequest() {
    }

    public AlertActionRequest(String actionNotes) {
        this.actionNotes = actionNotes;
    }

    public String getActionNotes() {
        return actionNotes;
    }

    public void setActionNotes(String actionNotes) {
        this.actionNotes = actionNotes;
    }
}
