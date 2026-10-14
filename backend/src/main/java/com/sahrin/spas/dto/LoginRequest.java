package com.sahrin.spas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "Email or identifier is required")
    @Size(max = 100, message = "Identifier or email must not exceed 100 characters")
    private String identifierOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String identifierOrEmail, String password) {
        this.identifierOrEmail = identifierOrEmail;
        this.password = password;
    }

    public String getIdentifierOrEmail() {
        return identifierOrEmail;
    }

    public void setIdentifierOrEmail(String identifierOrEmail) {
        this.identifierOrEmail = identifierOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
