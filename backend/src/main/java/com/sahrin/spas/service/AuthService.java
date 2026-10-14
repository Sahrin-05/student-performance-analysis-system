package com.sahrin.spas.service;

import com.sahrin.spas.dto.LoginRequest;
import com.sahrin.spas.dto.LoginResponse;
import com.sahrin.spas.dto.RegisterRequest;
import com.sahrin.spas.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
