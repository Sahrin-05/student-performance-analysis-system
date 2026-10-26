package com.sahrin.spas.service;

import com.sahrin.spas.dto.PageResponse;
import com.sahrin.spas.dto.UserResponse;
import com.sahrin.spas.dto.UserStatusUpdateRequest;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User.UserStatus;

import java.util.List;

public interface AdminService {

    List<UserResponse> getPendingUsers();

    UserResponse approveUser(Long id);

    UserResponse approveUser(Long id, String adminEmail);

    UserResponse rejectUser(Long id);

    UserResponse rejectUser(Long id, String adminEmail);

    List<UserResponse> getAllUsers(Role role, UserStatus status, String search);

    PageResponse<UserResponse> getAllUsersPaged(Role role, UserStatus status, String search, int page, int size);

    UserResponse getUserById(Long id);

    UserResponse updateUserStatus(Long id, UserStatusUpdateRequest request, String adminEmail);
}
