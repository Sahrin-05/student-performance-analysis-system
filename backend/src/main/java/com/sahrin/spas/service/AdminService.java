package com.sahrin.spas.service;

import com.sahrin.spas.dto.UserResponse;

import java.util.List;

public interface AdminService {

    List<UserResponse> getPendingUsers();

    UserResponse approveUser(Long id);

    UserResponse rejectUser(Long id);
}
