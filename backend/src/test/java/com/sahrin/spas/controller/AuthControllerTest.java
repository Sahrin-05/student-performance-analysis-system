package com.sahrin.spas.controller;

import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.LoginRequest;
import com.sahrin.spas.dto.LoginResponse;
import com.sahrin.spas.dto.RegisterRequest;
import com.sahrin.spas.dto.RegisterResponse;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.AccountPendingApprovalException;
import com.sahrin.spas.exception.DuplicateResourceException;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.exception.InvalidCredentialsException;
import com.sahrin.spas.exception.InvalidRegistrationException;
import com.sahrin.spas.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register with valid Student should return 201 Created and safe response")
    void register_student_valid_returns201() throws Exception {
        RegisterResponse response = new RegisterResponse(
                1L,
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                Role.STUDENT,
                UserStatus.PENDING_APPROVAL,
                "Account request submitted successfully. It is currently pending administrator approval."
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        String jsonPayload = """
            {
                "fullName": "Jane Doe",
                "identifier": "STU-2026-001",
                "email": "jane.doe@student.university.edu",
                "password": "Password123!",
                "role": "STUDENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.identifier").value("STU-2026-001"))
                .andExpect(jsonPath("$.email").value("jane.doe@student.university.edu"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register with valid Lecturer should return 201 Created")
    void register_lecturer_valid_returns201() throws Exception {
        RegisterResponse response = new RegisterResponse(
                2L,
                "Dr. John Smith",
                "STF-CS-001",
                "j.smith@faculty.university.edu",
                Role.LECTURER,
                UserStatus.PENDING_APPROVAL,
                "Account request submitted successfully. It is currently pending administrator approval."
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        String jsonPayload = """
            {
                "fullName": "Dr. John Smith",
                "identifier": "STF-CS-001",
                "email": "j.smith@faculty.university.edu",
                "password": "LecturerPass456!",
                "role": "LECTURER"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.role").value("LECTURER"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid fields should return 400 Bad Request with field errors")
    void register_invalidFields_returns400() throws Exception {
        String invalidJsonPayload = """
            {
                "fullName": "",
                "identifier": "AB",
                "email": "not-an-email",
                "password": "short",
                "role": null
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.identifier").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate email should return 409 Conflict")
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("An account with this email address already exists."));

        String jsonPayload = """
            {
                "fullName": "Jane Doe",
                "identifier": "STU-2026-001",
                "email": "jane.doe@student.university.edu",
                "password": "Password123!",
                "role": "STUDENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("An account with this email address already exists."));
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate identifier should return 409 Conflict")
    void register_duplicateIdentifier_returns409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("An account with this Student/Staff ID already exists."));

        String jsonPayload = """
            {
                "fullName": "Jane Doe",
                "identifier": "STU-2026-001",
                "email": "jane.doe@student.university.edu",
                "password": "Password123!",
                "role": "STUDENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("An account with this Student/Staff ID already exists."));
    }

    @Test
    @DisplayName("POST /api/auth/register with ADMIN role should return 400 Bad Request")
    void register_adminRole_returns400() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new InvalidRegistrationException("Self-registration for the ADMIN role is strictly prohibited."));

        String adminJsonPayload = """
            {
                "fullName": "Admin Attempt",
                "identifier": "ADM-001",
                "email": "admin@university.edu",
                "password": "AdminSecret123!",
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Self-registration for the ADMIN role is strictly prohibited."));
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials should return 200 OK and safe LoginResponse")
    void login_validCredentials_returns200() throws Exception {
        LoginResponse response = new LoginResponse(
                5L,
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                Role.STUDENT,
                UserStatus.ACTIVE,
                "Login successful."
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        String jsonPayload = """
            {
                "identifierOrEmail": "jane.doe@student.university.edu",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.identifier").value("STU-2026-001"))
                .andExpect(jsonPath("$.email").value("jane.doe@student.university.edu"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Login successful."))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid credentials should return 401 Unauthorized")
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email/identifier or password."));

        String jsonPayload = """
            {
                "identifierOrEmail": "jane.doe@student.university.edu",
                "password": "WrongPassword!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email/identifier or password."));
    }

    @Test
    @DisplayName("POST /api/auth/login with pending approval account should return 403 Forbidden")
    void login_pendingApproval_returns403() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AccountPendingApprovalException("Your account request is currently pending administrator approval. Please wait until your registration is approved."));

        String jsonPayload = """
            {
                "identifierOrEmail": "STU-2026-001",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Your account request is currently pending administrator approval. Please wait until your registration is approved."));
    }

    @Test
    @DisplayName("POST /api/auth/login with missing identifierOrEmail should return 400 Bad Request")
    void login_missingIdentifierOrEmail_returns400() throws Exception {
        String jsonPayload = """
            {
                "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.identifierOrEmail").value("Email or identifier is required"));
    }

    @Test
    @DisplayName("POST /api/auth/login with missing password should return 400 Bad Request")
    void login_missingPassword_returns400() throws Exception {
        String jsonPayload = """
            {
                "identifierOrEmail": "jane.doe@student.university.edu"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }
}
