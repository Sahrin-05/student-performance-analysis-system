package com.sahrin.spas.dto;

import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User.UserStatus;

public class LoginResponse {

    private Long id;
    private String fullName;
    private String identifier;
    private String email;
    private Role role;
    private UserStatus status;
    private String message;
    private String token;
    private String tokenType;

    public LoginResponse() {
    }

    public LoginResponse(Long id, String fullName, String identifier, String email, Role role, UserStatus status, String message) {
        this(id, fullName, identifier, email, role, status, message, null, null);
    }

    public LoginResponse(Long id, String fullName, String identifier, String email, Role role, UserStatus status, String message, String token, String tokenType) {
        this.id = id;
        this.fullName = fullName;
        this.identifier = identifier;
        this.email = email;
        this.role = role;
        this.status = status;
        this.message = message;
        this.token = token;
        this.tokenType = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
