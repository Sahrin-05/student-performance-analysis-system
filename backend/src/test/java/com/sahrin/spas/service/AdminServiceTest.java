package com.sahrin.spas.service;

import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.dto.UserStatusUpdateRequest;
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

    @Test
    @DisplayName("Should return filtered users using searchUsers")
    void getAllUsers_withFilters_returnsFilteredUsers() {
        User user1 = new User("Alice Green", "STU-001", "alice@university.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        user1.setId(10L);

        when(userRepository.searchUsers(Role.STUDENT, UserStatus.ACTIVE, "alice"))
                .thenReturn(List.of(user1));

        List<UserResponse> responses = adminService.getAllUsers(Role.STUDENT, UserStatus.ACTIVE, "alice");

        assertEquals(1, responses.size());
        assertEquals("Alice Green", responses.get(0).getFullName());
        assertEquals(Role.STUDENT, responses.get(0).getRole());
        assertEquals(UserStatus.ACTIVE, responses.get(0).getStatus());
    }

    @Test
    @DisplayName("Should retrieve user by id successfully")
    void getUserById_found_returnsResponse() {
        User user = new User("Dr. Robert Vance", "LEC-002", "vance@faculty.edu", "hash", Role.LECTURER, UserStatus.ACTIVE);
        user.setId(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        UserResponse response = adminService.getUserById(5L);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Dr. Robert Vance", response.getFullName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user id not found")
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(999L));
    }

    @Test
    @DisplayName("Should suspend an ACTIVE student successfully")
    void updateUserStatus_activeToSuspended_success() {
        User admin = new User("Admin", "ADM-001", "admin@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        User student = new User("Bob Smith", "STU-002", "bob@university.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student.setId(20L);

        when(userRepository.findById(20L)).thenReturn(Optional.of(student));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.SUSPENDED, "Academic probation");
        UserResponse response = adminService.updateUserStatus(20L, request, "admin@university.edu");

        assertEquals(UserStatus.SUSPENDED, response.getStatus());
        assertTrue(response.getMessage().contains("suspended successfully"));
        verify(userRepository).save(student);
    }

    @Test
    @DisplayName("Should reactivate a SUSPENDED user to ACTIVE")
    void updateUserStatus_suspendedToActive_success() {
        User admin = new User("Admin", "ADM-001", "admin@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        User student = new User("Bob Smith", "STU-002", "bob@university.edu", "hash", Role.STUDENT, UserStatus.SUSPENDED);
        student.setId(20L);

        when(userRepository.findById(20L)).thenReturn(Optional.of(student));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.ACTIVE, "Probation completed");
        UserResponse response = adminService.updateUserStatus(20L, request, "admin@university.edu");

        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertTrue(response.getMessage().contains("reactivated successfully"));
        verify(userRepository).save(student);
    }

    @Test
    @DisplayName("Should reinstate a REJECTED user to ACTIVE")
    void updateUserStatus_rejectedToActive_success() {
        User admin = new User("Admin", "ADM-001", "admin@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        User lecturer = new User("Dr. Clark", "LEC-003", "clark@faculty.edu", "hash", Role.LECTURER, UserStatus.REJECTED);
        lecturer.setId(30L);

        when(userRepository.findById(30L)).thenReturn(Optional.of(lecturer));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.ACTIVE, "Application appeal approved");
        UserResponse response = adminService.updateUserStatus(30L, request, "admin@university.edu");

        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertTrue(response.getMessage().contains("reinstated successfully"));
    }

    @Test
    @DisplayName("Should block administrator from modifying their own account status")
    void updateUserStatus_selfModification_throwsException() {
        User admin = new User("Admin", "ADM-001", "admin@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.SUSPENDED);

        InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.updateUserStatus(1L, request, "admin@university.edu")
        );

        assertTrue(ex.getMessage().contains("not permitted to modify their own account status"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should block administrator from modifying another ADMIN account")
    void updateUserStatus_adminTarget_throwsException() {
        User adminCaller = new User("Super Admin", "ADM-001", "super@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        adminCaller.setId(1L);

        User targetAdmin = new User("Secondary Admin", "ADM-002", "admin2@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        targetAdmin.setId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.findByEmail("super@university.edu")).thenReturn(Optional.of(adminCaller));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.SUSPENDED);

        InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.updateUserStatus(2L, request, "super@university.edu")
        );

        assertTrue(ex.getMessage().contains("Administrator accounts cannot have their status modified"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when transitioning to the same status")
    void updateUserStatus_sameStatus_throwsException() {
        User admin = new User("Admin", "ADM-001", "admin@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        User student = new User("Student", "STU-001", "student@university.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.ACTIVE);

        InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.updateUserStatus(10L, request, "admin@university.edu")
        );

        assertTrue(ex.getMessage().contains("already in status ACTIVE"));
    }

    @Test
    @DisplayName("Should throw when attempting to transition to PENDING_APPROVAL")
    void updateUserStatus_toPendingApproval_throwsException() {
        User admin = new User("Admin", "ADM-001", "admin@university.edu", "hash", Role.ADMIN, UserStatus.ACTIVE);
        admin.setId(1L);

        User student = new User("Student", "STU-001", "student@university.edu", "hash", Role.STUDENT, UserStatus.ACTIVE);
        student.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.PENDING_APPROVAL);

        InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> adminService.updateUserStatus(10L, request, "admin@university.edu")
        );

        assertTrue(ex.getMessage().contains("cannot be transitioned back to PENDING_APPROVAL"));
    }
}
