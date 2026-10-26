package com.sahrin.spas.controller;

import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.AdminAuditLogResponse;
import com.sahrin.spas.dto.PageResponse;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AdminAuditService;
import com.sahrin.spas.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminAuditController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAuditService adminAuditService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/audit-logs returns list when unpaged")
    void testGetAuditLogsList() throws Exception {
        AdminAuditLogResponse log1 = new AdminAuditLogResponse(
                1L, "USER_APPROVED", "USER", 5L, "STU001",
                "Approved STUDENT user account", 1L, "Admin User", "admin@spas.edu", LocalDateTime.now()
        );

        when(adminAuditService.getAuditLogs(null, null, null)).thenReturn(List.of(log1));

        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("USER_APPROVED"))
                .andExpect(jsonPath("$[0].entityIdentifier").value("STU001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/audit-logs with page/size returns PageResponse")
    void testGetAuditLogsPaged() throws Exception {
        AdminAuditLogResponse log1 = new AdminAuditLogResponse(
                1L, "USER_APPROVED", "USER", 5L, "STU001",
                "Approved STUDENT user account", 1L, "Admin User", "admin@spas.edu", LocalDateTime.now()
        );

        PageResponse<AdminAuditLogResponse> pageResponse = new PageResponse<>(
                List.of(log1), 0, 10, 1, 1, true, true
        );

        when(adminAuditService.getAuditLogsPaged(any(), any(), any(), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/audit-logs?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("USER_APPROVED"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/audit-logs with large size clamps size to 100")
    void testGetAuditLogsLargeSizeClamped() throws Exception {
        AdminAuditLogResponse log1 = new AdminAuditLogResponse(
                1L, "USER_APPROVED", "USER", 5L, "STU001",
                "Approved STUDENT user account", 1L, "Admin User", "admin@spas.edu", LocalDateTime.now()
        );

        PageResponse<AdminAuditLogResponse> pageResponse = new PageResponse<>(
                List.of(log1), 0, 100, 1, 1, true, true
        );

        when(adminAuditService.getAuditLogsPaged(any(), any(), any(), eq(0), eq(100)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/admin/audit-logs?page=0&size=250"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/audit-logs with negative page returns 400 Bad Request")
    void testGetAuditLogsNegativePage() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs?page=-2&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be negative."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/audit-logs with non-positive size returns 400 Bad Request")
    void testGetAuditLogsNonPositiveSize() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs?page=0&size=-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must be greater than zero."));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("GET /api/admin/audit-logs is forbidden for non-admin")
    void testGetAuditLogsForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
