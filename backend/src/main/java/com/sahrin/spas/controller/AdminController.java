package com.sahrin.spas.controller;

import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.dto.UserStatusUpdateRequest;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        if (page != null || size != null) {
            if (page != null && page < 0) {
                throw new IllegalArgumentException("Page index must not be negative.");
            }
            if (size != null && size <= 0) {
                throw new IllegalArgumentException("Page size must be greater than zero.");
            }
            int p = (page != null) ? page : 0;
            int s = (size != null) ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
            return ResponseEntity.ok(adminService.getAllUsersPaged(role, status, search, p, s));
        }
        List<UserResponse> users = adminService.getAllUsers(role, status, search);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        UserResponse response = adminService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication != null ? authentication.getName() : null;
        UserResponse response = adminService.updateUserStatus(id, request, adminEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserResponse>> getPendingUsers() {
        List<UserResponse> pendingUsers = adminService.getPendingUsers();
        return ResponseEntity.ok(pendingUsers);
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<UserResponse> approveUser(@PathVariable("id") Long id) {
        UserResponse response = adminService.approveUser(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<UserResponse> rejectUser(@PathVariable("id") Long id) {
        UserResponse response = adminService.rejectUser(id);
        return ResponseEntity.ok(response);
    }
}
