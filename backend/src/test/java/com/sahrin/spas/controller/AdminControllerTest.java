package com.sahrin.spas.controller;

import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.exception.InvalidStatusTransitionException;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AdminService;
import com.sahrin.spas.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private void mockAuthenticatedUser(Long userId, String email, Role role) {
        String token = "valid." + role.name().toLowerCase() + ".token";
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(jwtService.extractSubject(token)).thenReturn(email);

        User user = new User("Auth User", "ID-" + userId, email, "$2a$10$hash", role, UserStatus.ACTIVE);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("GET /api/admin/users/pending with ADMIN token should return 200 OK and list of pending users")
    void getPendingUsers_admin_returns200() throws Exception {
        mockAuthenticatedUser(1L, "admin@university.edu", Role.ADMIN);

        UserResponse user1 = new UserResponse(
                10L,
                "Jane Doe",
                "STU-001",
                "jane.doe@student.university.edu",
                Role.STUDENT,
                UserStatus.PENDING_APPROVAL
        );
        UserResponse user2 = new UserResponse(
                11L,
                "Dr. John Smith",
                "LEC-001",
                "john.smith@faculty.university.edu",
                Role.LECTURER,
                UserStatus.PENDING_APPROVAL
        );

        when(adminService.getPendingUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/admin/users/pending")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].fullName").value("Jane Doe"))
                .andExpect(jsonPath("$[0].identifier").value("STU-001"))
                .andExpect(jsonPath("$[0].email").value("jane.doe@student.university.edu"))
                .andExpect(jsonPath("$[0].role").value("STUDENT"))
                .andExpect(jsonPath("$[0].status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].role").value("LECTURER"));
    }

    @Test
    @DisplayName("GET /api/admin/users/pending when unauthenticated should return 403 Forbidden")
    void getPendingUsers_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/users/pending with STUDENT token should return 403 Forbidden")
    void getPendingUsers_studentRole_returnsForbidden() throws Exception {
        mockAuthenticatedUser(2L, "student@university.edu", Role.STUDENT);

        mockMvc.perform(get("/api/admin/users/pending")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/users/pending with LECTURER token should return 403 Forbidden")
    void getPendingUsers_lecturerRole_returnsForbidden() throws Exception {
        mockAuthenticatedUser(3L, "lecturer@university.edu", Role.LECTURER);

        mockMvc.perform(get("/api/admin/users/pending")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/approve with ADMIN token should return 200 OK and updated UserResponse")
    void approveUser_admin_returns200() throws Exception {
        mockAuthenticatedUser(1L, "admin@university.edu", Role.ADMIN);

        UserResponse response = new UserResponse(
                10L,
                "Jane Doe",
                "STU-001",
                "jane.doe@student.university.edu",
                Role.STUDENT,
                UserStatus.ACTIVE,
                "User account approved successfully."
        );

        when(adminService.approveUser(10L)).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/10/approve")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("User account approved successfully."))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/approve when unauthenticated should return 403 Forbidden")
    void approveUser_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(put("/api/admin/users/10/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/approve with STUDENT token should return 403 Forbidden")
    void approveUser_studentRole_returnsForbidden() throws Exception {
        mockAuthenticatedUser(2L, "student@university.edu", Role.STUDENT);

        mockMvc.perform(put("/api/admin/users/10/approve")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/approve with nonexistent ID should return 404 Not Found")
    void approveUser_notFound_returns404() throws Exception {
        mockAuthenticatedUser(1L, "admin@university.edu", Role.ADMIN);

        when(adminService.approveUser(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(put("/api/admin/users/99/approve")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/approve with invalid status transition should return 400 Bad Request")
    void approveUser_invalidTransition_returns400() throws Exception {
        mockAuthenticatedUser(1L, "admin@university.edu", Role.ADMIN);

        when(adminService.approveUser(10L))
                .thenThrow(new InvalidStatusTransitionException("Cannot approve user. Only accounts with PENDING_APPROVAL status can be approved. Current status: ACTIVE"));

        mockMvc.perform(put("/api/admin/users/10/approve")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot approve user. Only accounts with PENDING_APPROVAL status can be approved. Current status: ACTIVE"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/reject with ADMIN token should return 200 OK and updated UserResponse")
    void rejectUser_admin_returns200() throws Exception {
        mockAuthenticatedUser(1L, "admin@university.edu", Role.ADMIN);

        UserResponse response = new UserResponse(
                20L,
                "Dr. John Smith",
                "LEC-001",
                "john.smith@faculty.university.edu",
                Role.LECTURER,
                UserStatus.REJECTED,
                "User account rejected successfully."
        );

        when(adminService.rejectUser(20L)).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/20/reject")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.fullName").value("Dr. John Smith"))
                .andExpect(jsonPath("$.role").value("LECTURER"))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.message").value("User account rejected successfully."))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/reject when unauthenticated should return 403 Forbidden")
    void rejectUser_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(put("/api/admin/users/20/reject"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/reject with LECTURER token should return 403 Forbidden")
    void rejectUser_lecturerRole_returnsForbidden() throws Exception {
        mockAuthenticatedUser(3L, "lecturer@university.edu", Role.LECTURER);

        mockMvc.perform(put("/api/admin/users/20/reject")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/reject with nonexistent ID should return 404 Not Found")
    void rejectUser_notFound_returns404() throws Exception {
        mockAuthenticatedUser(1L, "admin@university.edu", Role.ADMIN);

        when(adminService.rejectUser(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(put("/api/admin/users/99/reject")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/reject with invalid status transition should return 400 Bad Request")
    void rejectUser_invalidTransition_returns400() throws Exception {
        mockAuthenticatedUser(1L, "admin@university.edu", Role.ADMIN);

        when(adminService.rejectUser(20L))
                .thenThrow(new InvalidStatusTransitionException("Cannot reject user. Only accounts with PENDING_APPROVAL status can be rejected. Current status: REJECTED"));

        mockMvc.perform(put("/api/admin/users/20/reject")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot reject user. Only accounts with PENDING_APPROVAL status can be rejected. Current status: REJECTED"));
    }
}
