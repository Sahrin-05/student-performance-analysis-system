package com.sahrin.spas.service.impl;

import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.dto.UserStatusUpdateRequest;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.InvalidStatusTransitionException;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    public AdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers() {
        List<User> pendingUsers = userRepository.findByStatusOrderByIdAsc(UserStatus.PENDING_APPROVAL);
        return pendingUsers.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Role role, UserStatus status, String search) {
        String query = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<User> users = userRepository.searchUsers(role, status, query);
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("User ID must not be null.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, UserStatusUpdateRequest request, String adminEmail) {
        if (id == null) {
            throw new ResourceNotFoundException("User ID must not be null.");
        }
        if (request == null || request.getStatus() == null) {
            throw new InvalidStatusTransitionException("Target user status must not be null.");
        }

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Prevent admin self-modification / self-lockout
        if (adminEmail != null && !adminEmail.trim().isEmpty()) {
            User currentAdmin = userRepository.findByEmail(adminEmail.trim().toLowerCase())
                    .orElse(null);
            if (currentAdmin != null && (targetUser.getId().equals(currentAdmin.getId())
                    || targetUser.getEmail().equalsIgnoreCase(currentAdmin.getEmail()))) {
                throw new InvalidStatusTransitionException("Administrators are not permitted to modify their own account status.");
            }
        }

        // Prevent modification of other administrator accounts
        if (targetUser.getRole() == Role.ADMIN) {
            throw new InvalidStatusTransitionException("Administrator accounts cannot have their status modified.");
        }

        UserStatus currentStatus = targetUser.getStatus();
        UserStatus newStatus = request.getStatus();

        if (currentStatus == newStatus) {
            throw new InvalidStatusTransitionException("User is already in status " + currentStatus + ".");
        }

        if (newStatus == UserStatus.PENDING_APPROVAL) {
            throw new InvalidStatusTransitionException("Accounts cannot be transitioned back to PENDING_APPROVAL.");
        }

        if (currentStatus == UserStatus.PENDING_APPROVAL && newStatus == UserStatus.SUSPENDED) {
            throw new InvalidStatusTransitionException("An unapproved pending applicant cannot be directly suspended. Approve or reject first.");
        }

        if (currentStatus == UserStatus.REJECTED && newStatus == UserStatus.SUSPENDED) {
            throw new InvalidStatusTransitionException("A rejected user cannot be directly suspended. Reinstate to ACTIVE first.");
        }

        // Apply new status while preserving existing role and credentials
        targetUser.setStatus(newStatus);
        User savedUser = userRepository.save(targetUser);

        String message = switch (newStatus) {
            case ACTIVE -> currentStatus == UserStatus.SUSPENDED
                    ? "Account reactivated successfully. The user can now access the system."
                    : currentStatus == UserStatus.REJECTED
                    ? "Account reinstated successfully. Status set to ACTIVE."
                    : "Account approved successfully.";
            case SUSPENDED -> "Account suspended successfully. Active sessions revoked.";
            case REJECTED -> "Account rejected successfully.";
            case PENDING_APPROVAL -> "Account status updated.";
        };

        return toResponse(savedUser, message);
    }

    @Override
    @Transactional
    public UserResponse approveUser(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("User ID must not be null.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new InvalidStatusTransitionException("Administrator accounts cannot be modified via approval endpoints.");
        }

        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "Cannot approve user. Only accounts with PENDING_APPROVAL status can be approved. Current status: " + user.getStatus()
            );
        }

        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        return toResponse(savedUser, "User account approved successfully.");
    }

    @Override
    @Transactional
    public UserResponse rejectUser(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("User ID must not be null.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new InvalidStatusTransitionException("Administrator accounts cannot be modified via rejection endpoints.");
        }

        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "Cannot reject user. Only accounts with PENDING_APPROVAL status can be rejected. Current status: " + user.getStatus()
            );
        }

        user.setStatus(UserStatus.REJECTED);
        User savedUser = userRepository.save(user);

        return toResponse(savedUser, "User account rejected successfully.");
    }

    private UserResponse toResponse(User user) {
        return toResponse(user, null);
    }

    private UserResponse toResponse(User user, String message) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getIdentifier(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                message
        );
    }
}
