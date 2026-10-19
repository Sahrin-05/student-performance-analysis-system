package com.sahrin.spas.service.impl;

import com.sahrin.spas.dto.LoginRequest;
import com.sahrin.spas.dto.LoginResponse;
import com.sahrin.spas.dto.RegisterRequest;
import com.sahrin.spas.dto.RegisterResponse;
import com.sahrin.spas.dto.StatusCheckRequest;
import com.sahrin.spas.dto.StatusCheckResponse;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.AccountPendingApprovalException;
import com.sahrin.spas.exception.AccountStatusException;
import com.sahrin.spas.exception.DuplicateResourceException;
import com.sahrin.spas.exception.InvalidCredentialsException;
import com.sahrin.spas.exception.InvalidRegistrationException;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AuthService;
import com.sahrin.spas.service.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. Role validation: Allow only STUDENT and LECTURER self-registration
        if (request.getRole() == Role.ADMIN) {
            throw new InvalidRegistrationException("Self-registration for the ADMIN role is strictly prohibited.");
        }
        if (request.getRole() != Role.STUDENT && request.getRole() != Role.LECTURER) {
            throw new InvalidRegistrationException("Invalid registration role. Only STUDENT and LECTURER are permitted.");
        }

        // 2. Duplicate checks for email and identifier
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedIdentifier = request.getIdentifier().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email address already exists.");
        }

        if (userRepository.existsByIdentifier(normalizedIdentifier)) {
            throw new DuplicateResourceException("An account with this Student/Staff ID already exists.");
        }

        // 3. Password hashing using BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 4. Create user with server-assigned PENDING_APPROVAL status
        User user = new User(
                request.getFullName().trim(),
                normalizedIdentifier,
                normalizedEmail,
                hashedPassword,
                request.getRole(),
                UserStatus.PENDING_APPROVAL
        );

        // 5. Save with defensive handling for database-level unique constraint race conditions
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("User with this email or identifier already exists.");
        }

        // 6. Return safe response without passwordHash
        return new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getIdentifier(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                "Account request submitted successfully. It is currently pending administrator approval."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String input = request.getIdentifierOrEmail().trim();

        // 1. Look up user by email (case-insensitive) or by identifier
        Optional<User> userOptional = userRepository.findByEmail(input.toLowerCase());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByIdentifier(input);
        }

        // 2. Safe generic rejection if user does not exist
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Invalid email/identifier or password.");
        }

        User user = userOptional.get();

        // 3. Verify password securely using BCrypt PasswordEncoder
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email/identifier or password.");
        }

        // 4. Validate account eligibility based on status
        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new AccountPendingApprovalException(
                    "Your account request is currently pending administrator approval. Please wait until your registration is approved."
            );
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new AccountStatusException(
                    "Your account request has been rejected by an administrator. Please contact faculty administration for assistance."
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountStatusException("Your account is not active. Please contact administrator.");
        }

        // 5. Establish authentication in Spring SecurityContext
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 6. Generate JWT token for the authenticated active user
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        // 7. Return safe authenticated user details and token (no password or hash)
        return new LoginResponse(
                user.getId(),
                user.getFullName(),
                user.getIdentifier(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                "Login successful.",
                token,
                "Bearer"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StatusCheckResponse checkStatus(StatusCheckRequest request) {
        if (request == null || request.getIdentifier() == null || request.getInstitutionalEmail() == null) {
            throw new ResourceNotFoundException("No registration request found matching the provided identifier and email.");
        }

        String normalizedIdentifier = request.getIdentifier().trim();
        String normalizedEmail = request.getInstitutionalEmail().trim().toLowerCase();

        Optional<User> userOptional = userRepository.findByIdentifierIgnoreCaseAndEmailIgnoreCase(
                normalizedIdentifier,
                normalizedEmail
        );

        if (userOptional.isEmpty() || userOptional.get().getRole() == Role.ADMIN) {
            throw new ResourceNotFoundException("No registration request found matching the provided identifier and email.");
        }

        User user = userOptional.get();

        String message = switch (user.getStatus()) {
            case PENDING_APPROVAL -> "Your account request is currently pending administrator approval.";
            case ACTIVE -> "Your account has been approved and is active. You may now sign in.";
            case REJECTED -> "Your account registration request has been rejected. Please contact faculty administration for assistance.";
        };

        return new StatusCheckResponse(user.getStatus(), message);
    }
}
