package com.sahrin.spas.service;

import com.sahrin.spas.dto.LoginRequest;
import com.sahrin.spas.dto.LoginResponse;
import com.sahrin.spas.dto.RegisterRequest;
import com.sahrin.spas.dto.RegisterResponse;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.AccountPendingApprovalException;
import com.sahrin.spas.exception.AccountStatusException;
import com.sahrin.spas.exception.DuplicateResourceException;
import com.sahrin.spas.exception.InvalidCredentialsException;
import com.sahrin.spas.exception.InvalidRegistrationException;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.AuthServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest studentRequest;
    private RegisterRequest lecturerRequest;

    @BeforeEach
    void setUp() {
        studentRequest = new RegisterRequest(
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                "Password123!",
                Role.STUDENT
        );

        lecturerRequest = new RegisterRequest(
                "Dr. John Smith",
                "STF-CS-001",
                "j.smith@faculty.university.edu",
                "LecturerPass456!",
                Role.LECTURER
        );
    }

    @Test
    @DisplayName("Should successfully register a student with hashed password and PENDING_APPROVAL status")
    void register_student_success() {
        when(userRepository.existsByEmail("jane.doe@student.university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("STU-2026-001")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedPasswordSample");

        User savedUser = new User(
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                "$2a$10$hashedPasswordSample",
                Role.STUDENT,
                UserStatus.PENDING_APPROVAL
        );
        savedUser.setId(101L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(studentRequest);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Jane Doe", response.getFullName());
        assertEquals("STU-2026-001", response.getIdentifier());
        assertEquals("jane.doe@student.university.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals(UserStatus.PENDING_APPROVAL, response.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotEquals("Password123!", capturedUser.getPasswordHash(), "Plaintext password must never be stored");
        assertEquals("$2a$10$hashedPasswordSample", capturedUser.getPasswordHash());
        assertEquals(UserStatus.PENDING_APPROVAL, capturedUser.getStatus(), "Status must be PENDING_APPROVAL");
    }

    @Test
    @DisplayName("Should successfully register a lecturer with PENDING_APPROVAL status")
    void register_lecturer_success() {
        when(userRepository.existsByEmail("j.smith@faculty.university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("STF-CS-001")).thenReturn(false);
        when(passwordEncoder.encode("LecturerPass456!")).thenReturn("$2a$10$hashedLecturerPassword");

        User savedUser = new User(
                "Dr. John Smith",
                "STF-CS-001",
                "j.smith@faculty.university.edu",
                "$2a$10$hashedLecturerPassword",
                Role.LECTURER,
                UserStatus.PENDING_APPROVAL
        );
        savedUser.setId(202L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(lecturerRequest);

        assertNotNull(response);
        assertEquals(202L, response.getId());
        assertEquals(Role.LECTURER, response.getRole());
        assertEquals(UserStatus.PENDING_APPROVAL, response.getStatus());
    }

    @Test
    @DisplayName("Should reject self-registration attempt with ADMIN role")
    void register_adminRole_throwsInvalidRegistrationException() {
        RegisterRequest adminRequest = new RegisterRequest(
                "Admin User",
                "ADM-001",
                "admin@university.edu",
                "AdminSecret999!",
                Role.ADMIN
        );

        InvalidRegistrationException exception = assertThrows(
                InvalidRegistrationException.class,
                () -> authService.register(adminRequest)
        );

        assertEquals("Self-registration for the ADMIN role is strictly prohibited.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email is already in use")
    void register_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("jane.doe@student.university.edu")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(studentRequest)
        );

        assertEquals("An account with this email address already exists.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when identifier is already in use")
    void register_duplicateIdentifier_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("jane.doe@student.university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("STU-2026-001")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(studentRequest)
        );

        assertEquals("An account with this Student/Staff ID already exists.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should safely handle database unique constraint violation during race conditions")
    void register_databaseConstraintViolation_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("jane.doe@student.university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("STU-2026-001")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("someHash");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(studentRequest)
        );

        assertEquals("User with this email or identifier already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully authenticate user with email and return LoginResponse")
    void login_success_withEmail() {
        User activeUser = new User(
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.ACTIVE
        );
        activeUser.setId(10L);

        when(userRepository.findByEmail("jane.doe@student.university.edu")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password123!", "$2a$10$encodedHash")).thenReturn(true);

        LoginRequest request = new LoginRequest("jane.doe@student.university.edu", "Password123!");
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Jane Doe", response.getFullName());
        assertEquals("STU-2026-001", response.getIdentifier());
        assertEquals("jane.doe@student.university.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertEquals("Login successful.", response.getMessage());
    }

    @Test
    @DisplayName("Should successfully authenticate user with student identifier and return LoginResponse")
    void login_success_withIdentifier() {
        User activeUser = new User(
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.ACTIVE
        );
        activeUser.setId(10L);

        when(userRepository.findByEmail("stu-2026-001")).thenReturn(Optional.empty());
        when(userRepository.findByIdentifier("STU-2026-001")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password123!", "$2a$10$encodedHash")).thenReturn(true);

        LoginRequest request = new LoginRequest("STU-2026-001", "Password123!");
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Jane Doe", response.getFullName());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException with safe generic message when user does not exist")
    void login_userNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail("nonexistent@university.edu")).thenReturn(Optional.empty());
        when(userRepository.findByIdentifier("nonexistent@university.edu")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nonexistent@university.edu", "Password123!");

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email/identifier or password.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException with safe generic message when password is wrong")
    void login_wrongPassword_throwsInvalidCredentialsException() {
        User activeUser = new User(
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.ACTIVE
        );

        when(userRepository.findByEmail("jane.doe@student.university.edu")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongPassword!", "$2a$10$encodedHash")).thenReturn(false);

        LoginRequest request = new LoginRequest("jane.doe@student.university.edu", "WrongPassword!");

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email/identifier or password.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw AccountPendingApprovalException when account status is PENDING_APPROVAL")
    void login_pendingApproval_throwsAccountPendingApprovalException() {
        User pendingUser = new User(
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.PENDING_APPROVAL
        );

        when(userRepository.findByEmail("jane.doe@student.university.edu")).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.matches("Password123!", "$2a$10$encodedHash")).thenReturn(true);

        LoginRequest request = new LoginRequest("jane.doe@student.university.edu", "Password123!");

        AccountPendingApprovalException exception = assertThrows(
                AccountPendingApprovalException.class,
                () -> authService.login(request)
        );

        assertEquals("Your account request is currently pending administrator approval. Please wait until your registration is approved.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw AccountStatusException when account status is REJECTED")
    void login_rejectedStatus_throwsAccountStatusException() {
        User rejectedUser = new User(
                "Jane Doe",
                "STU-2026-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.REJECTED
        );

        when(userRepository.findByEmail("jane.doe@student.university.edu")).thenReturn(Optional.of(rejectedUser));
        when(passwordEncoder.matches("Password123!", "$2a$10$encodedHash")).thenReturn(true);

        LoginRequest request = new LoginRequest("jane.doe@student.university.edu", "Password123!");

        AccountStatusException exception = assertThrows(
                AccountStatusException.class,
                () -> authService.login(request)
        );

        assertEquals("Your account request has been rejected by an administrator. Please contact faculty administration for assistance.", exception.getMessage());
    }
}
