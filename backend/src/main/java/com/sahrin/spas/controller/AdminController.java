package com.sahrin.spas.controller;

import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
