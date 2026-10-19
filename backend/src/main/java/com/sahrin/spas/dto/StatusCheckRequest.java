package com.sahrin.spas.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StatusCheckRequest {

    @NotBlank(message = "Student or Staff ID is required")
    @Size(min = 3, max = 50, message = "Identifier must be between 3 and 50 characters")
    private String identifier;

    @NotBlank(message = "Institutional email is required")
    @Email(message = "Please provide a valid institutional email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @JsonAlias({"email", "institutionalEmail"})
    private String institutionalEmail;

    public StatusCheckRequest() {
    }

    public StatusCheckRequest(String identifier, String institutionalEmail) {
        this.identifier = identifier;
        this.institutionalEmail = institutionalEmail;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }
}
