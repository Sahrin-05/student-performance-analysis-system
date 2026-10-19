package com.sahrin.spas.service;

import com.sahrin.spas.dto.LoginRequest;
import com.sahrin.spas.dto.LoginResponse;
import com.sahrin.spas.dto.RegisterRequest;
import com.sahrin.spas.dto.RegisterResponse;
import com.sahrin.spas.dto.StatusCheckRequest;
import com.sahrin.spas.dto.StatusCheckResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    StatusCheckResponse checkStatus(StatusCheckRequest request);
}
