package com.sahrin.spas.service;

import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.InvalidStatusTransitionException;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("Should return list of pending users mapped to safe UserResponse")
    void getPendingUsers_returnsList() {
        User pendingStudent = new User(
                "Jane Doe",
                "STU-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.PENDING_APPROVAL
        );
        pendingStudent.setId(1L);

        User pendingLecturer = new User(
                "Dr. John Smith",
                "LEC-001",
                "john.smith@faculty.university.edu",
                "$2a$10$encodedHash",
                Role.LECTURER,
                UserStatus.PENDING_APPROVAL
        );
        pendingLecturer.setId(2L);

        when(userRepository.findByStatusOrderByIdAsc(UserStatus.PENDING_APPROVAL))
                .thenReturn(List.of(pendingStudent, pendingLecturer));

        List<UserResponse> responses = adminService.getPendingUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        UserResponse first = responses.get(0);
        assertEquals(1L, first.getId());
        assertEquals("Jane Doe", first.getFullName());
        assertEquals("STU-001", first.getIdentifier());
        assertEquals("jane.doe@student.university.edu", first.getEmail());
        assertEquals(Role.STUDENT, first.getRole());
        assertEquals(UserStatus.PENDING_APPROVAL, first.getStatus());
        assertNull(first.getMessage());

        UserResponse second = responses.get(1);
        assertEquals(2L, second.getId());
        assertEquals("Dr. John Smith", second.getFullName());
        assertEquals(Role.LECTURER, second.getRole());
    }

    @Test
    @DisplayName("Should return empty list when no users are pending approval")
    void getPendingUsers_empty_returnsEmptyList() {
        when(userRepository.findByStatusOrderByIdAsc(UserStatus.PENDING_APPROVAL))
                .thenReturn(List.of());

        List<UserResponse> responses = adminService.getPendingUsers();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should successfully approve a PENDING_APPROVAL user, change status to ACTIVE, and preserve role")
    void approveUser_success() {
        User pendingUser = new User(
                "Jane Doe",
                "STU-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.PENDING_APPROVAL
        );
        pendingUser.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = adminService.approveUser(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(Role.STUDENT, response.getRole(), "User role must be preserved");
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertEquals("User account approved successfully.", response.getMessage());

        assertEquals(UserStatus.ACTIVE, pendingUser.getStatus());
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when approving nonexistent user")
    void approveUser_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.approveUser(99L)
        );

        assertEquals("User not found with id: 99", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when approving an already ACTIVE user")
    void approveUser_alreadyActive_throwsException() {
        User activeUser = new User(
                "Jane Doe",
                "STU-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.ACTIVE
        );
        activeUser.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.approveUser(10L)
        );

        assertTrue(exception.getMessage().contains("Only accounts with PENDING_APPROVAL status can be approved"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when approving an already REJECTED user")
    void approveUser_alreadyRejected_throwsException() {
        User rejectedUser = new User(
                "Jane Doe",
                "STU-001",
                "jane.doe@student.university.edu",
                "$2a$10$encodedHash",
                Role.STUDENT,
                UserStatus.REJECTED
        );
        rejectedUser.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(rejectedUser));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.approveUser(10L)
        );

        assertTrue(exception.getMessage().contains("Only accounts with PENDING_APPROVAL status can be approved"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when attempting to approve an ADMIN account")
    void approveUser_adminAccount_throwsException() {
        User adminUser = new User(
                "System Admin",
                "ADMIN001",
                "admin@university.edu",
                "$2a$10$encodedHash",
                Role.ADMIN,
                UserStatus.PENDING_APPROVAL
        );
        adminUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.approveUser(1L)
        );

        assertEquals("Administrator accounts cannot be modified via approval endpoints.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully reject a PENDING_APPROVAL user, change status to REJECTED, and preserve role")
    void rejectUser_success() {
        User pendingUser = new User(
                "Dr. John Smith",
                "LEC-001",
                "john.smith@faculty.university.edu",
                "$2a$10$encodedHash",
                Role.LECTURER,
                UserStatus.PENDING_APPROVAL
        );
        pendingUser.setId(20L);

        when(userRepository.findById(20L)).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = adminService.rejectUser(20L);

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals(Role.LECTURER, response.getRole(), "User role must be preserved");
        assertEquals(UserStatus.REJECTED, response.getStatus());
        assertEquals("User account rejected successfully.", response.getMessage());

        assertEquals(UserStatus.REJECTED, pendingUser.getStatus());
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when rejecting nonexistent user")
    void rejectUser_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.rejectUser(99L)
        );

        assertEquals("User not found with id: 99", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when rejecting an already ACTIVE user")
    void rejectUser_alreadyActive_throwsException() {
        User activeUser = new User(
                "Dr. John Smith",
                "LEC-001",
                "john.smith@faculty.university.edu",
                "$2a$10$encodedHash",
                Role.LECTURER,
                UserStatus.ACTIVE
        );
        activeUser.setId(20L);

        when(userRepository.findById(20L)).thenReturn(Optional.of(activeUser));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.rejectUser(20L)
        );

        assertTrue(exception.getMessage().contains("Only accounts with PENDING_APPROVAL status can be rejected"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when rejecting an already REJECTED user")
    void rejectUser_alreadyRejected_throwsException() {
        User rejectedUser = new User(
                "Dr. John Smith",
                "LEC-001",
                "john.smith@faculty.university.edu",
                "$2a$10$encodedHash",
                Role.LECTURER,
                UserStatus.REJECTED
        );
        rejectedUser.setId(20L);

        when(userRepository.findById(20L)).thenReturn(Optional.of(rejectedUser));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.rejectUser(20L)
        );

        assertTrue(exception.getMessage().contains("Only accounts with PENDING_APPROVAL status can be rejected"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when attempting to reject an ADMIN account")
    void rejectUser_adminAccount_throwsException() {
        User adminUser = new User(
                "System Admin",
                "ADMIN001",
                "admin@university.edu",
                "$2a$10$encodedHash",
                Role.ADMIN,
                UserStatus.PENDING_APPROVAL
        );
        adminUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.rejectUser(1L)
        );

        assertEquals("Administrator accounts cannot be modified via rejection endpoints.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
