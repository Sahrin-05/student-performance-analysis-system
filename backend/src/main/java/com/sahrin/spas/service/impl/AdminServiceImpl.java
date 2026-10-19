package com.sahrin.spas.service.impl;

import com.sahrin.spas.dto.UserResponse;
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
