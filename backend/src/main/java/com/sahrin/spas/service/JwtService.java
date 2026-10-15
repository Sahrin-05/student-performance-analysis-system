package com.sahrin.spas.service;

import java.util.Date;
import java.util.Map;

public interface JwtService {

    String generateToken(String subject, String role);

    String generateToken(Long userId, String subject, String role);

    String generateToken(Long userId, String subject, String role, Map<String, Object> extraClaims);

    boolean validateToken(String token);

    String extractSubject(String token);

    String extractRole(String token);

    Long extractUserId(String token);

    Date extractExpiration(String token);

    boolean isTokenExpired(String token);
}
