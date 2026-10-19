package com.sahrin.spas.dto;

import com.sahrin.spas.entity.User.UserStatus;

public class StatusCheckResponse {

    private UserStatus status;
    private String message;

    public StatusCheckResponse() {
    }

    public StatusCheckResponse(UserStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
