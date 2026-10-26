package com.sahrin.spas.controller;

import com.sahrin.spas.dto.AdminAuditLogResponse;
import com.sahrin.spas.dto.PageResponse;
import com.sahrin.spas.service.AdminAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    public AdminAuditController(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "entityType", required = false) String entityType,
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
            PageResponse<AdminAuditLogResponse> paged = adminAuditService.getAuditLogsPaged(action, entityType, search, p, s);
            return ResponseEntity.ok(paged);
        }

        List<AdminAuditLogResponse> logs = adminAuditService.getAuditLogs(action, entityType, search);
        return ResponseEntity.ok(logs);
    }
}
