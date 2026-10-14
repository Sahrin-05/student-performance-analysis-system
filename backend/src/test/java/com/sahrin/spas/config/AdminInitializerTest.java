package com.sahrin.spas.config;

import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should successfully seed Admin when valid email and password are provided")
    void run_withValidCredentials_createsAdminSuccessfully() {
        AdminInitializer initializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@university.edu",
                "SuperAdminSecret123!",
                "ADM-001",
                "Chief Administrator"
        );

        when(userRepository.existsByEmail("admin@university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("ADM-001")).thenReturn(false);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("SuperAdminSecret123!")).thenReturn("$2a$10$hashedAdminPassword");

        initializer.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedAdmin = userCaptor.getValue();
        assertNotNull(savedAdmin);
        assertEquals("admin@university.edu", savedAdmin.getEmail());
        assertEquals("ADM-001", savedAdmin.getIdentifier());
        assertEquals("Chief Administrator", savedAdmin.getFullName());
        assertEquals("$2a$10$hashedAdminPassword", savedAdmin.getPasswordHash());
        assertEquals(Role.ADMIN, savedAdmin.getRole());
        assertEquals(UserStatus.ACTIVE, savedAdmin.getStatus());
    }

    @Test
    @DisplayName("Should use default identifier and full name when not explicitly provided")
    void run_withDefaults_usesStandardIdentifierAndFullName() {
        AdminInitializer initializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@university.edu",
                "SuperAdminSecret123!",
                "",
                ""
        );

        when(userRepository.existsByEmail("admin@university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("ADMIN001")).thenReturn(false);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("SuperAdminSecret123!")).thenReturn("$2a$10$hashedAdminPassword");

        initializer.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedAdmin = userCaptor.getValue();
        assertEquals("ADMIN001", savedAdmin.getIdentifier());
        assertEquals("System Administrator", savedAdmin.getFullName());
    }

    @Test
    @DisplayName("Should safely skip Admin seeding when ADMIN_EMAIL is missing or empty")
    void run_withMissingEmail_skipsSeeding() {
        AdminInitializer initializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "",
                "SuperAdminSecret123!",
                "ADM-001",
                "Chief Administrator"
        );

        initializer.run();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should safely skip Admin seeding when ADMIN_PASSWORD is missing or empty")
    void run_withMissingPassword_skipsSeeding() {
        AdminInitializer initializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@university.edu",
                "   ",
                "ADM-001",
                "Chief Administrator"
        );

        initializer.run();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should be idempotent: skips seeding when account with admin email already exists")
    void run_whenEmailAlreadyExists_skipsSeeding() {
        AdminInitializer initializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@university.edu",
                "SuperAdminSecret123!",
                "ADM-001",
                "Chief Administrator"
        );

        when(userRepository.existsByEmail("admin@university.edu")).thenReturn(true);

        initializer.run();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should be idempotent: skips seeding when account with identifier already exists")
    void run_whenIdentifierAlreadyExists_skipsSeeding() {
        AdminInitializer initializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@university.edu",
                "SuperAdminSecret123!",
                "ADM-001",
                "Chief Administrator"
        );

        when(userRepository.existsByEmail("admin@university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("ADM-001")).thenReturn(true);

        initializer.run();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should be idempotent: skips seeding when an ADMIN role account already exists")
    void run_whenAdminRoleAlreadyExists_skipsSeeding() {
        AdminInitializer initializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@university.edu",
                "SuperAdminSecret123!",
                "ADM-001",
                "Chief Administrator"
        );

        when(userRepository.existsByEmail("admin@university.edu")).thenReturn(false);
        when(userRepository.existsByIdentifier("ADM-001")).thenReturn(false);
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        initializer.run();

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
