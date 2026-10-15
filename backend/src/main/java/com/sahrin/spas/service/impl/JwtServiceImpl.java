package com.sahrin.spas.service.impl;

import com.sahrin.spas.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);
    private static final int MIN_KEY_BYTES = 32; // 256 bits for HMAC-SHA256

    private static final Set<String> RESERVED_CLAIM_NAMES = Set.of(
            "sub",
            "iat",
            "exp",
            "nbf",
            "iss",
            "aud",
            "jti",
            "userid",
            "role"
    );

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtServiceImpl(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JWT configuration error: Secret key is not configured. Please set the JWT_SECRET environment variable."
            );
        }

        byte[] keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalArgumentException(
                    "JWT configuration error: Secret key must be at least 256 bits (32 bytes) for HMAC-SHA256. Provided key length: "
                            + keyBytes.length + " bytes."
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalArgumentException(
                    "JWT configuration error: Token expiration time must be greater than zero."
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        log.info("JwtService initialized successfully with HMAC-SHA256 signing and expiration {} ms.", expirationMs);
    }

    @Override
    public String generateToken(String subject, String role) {
        return generateToken(null, subject, role, null);
    }

    @Override
    public String generateToken(Long userId, String subject, String role) {
        return generateToken(userId, subject, role, null);
    }

    @Override
    public String generateToken(Long userId, String subject, String role, Map<String, Object> extraClaims) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token generation failed: Subject cannot be null or empty.");
        }

        long nowMillis = System.currentTimeMillis();
        Date issuedAt = new Date(nowMillis);
        Date expiration = new Date(nowMillis + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        if (extraClaims != null) {
            for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
                String key = entry.getKey();
                if (key != null && !RESERVED_CLAIM_NAMES.contains(key.trim().toLowerCase(Locale.ROOT))) {
                    claims.put(key, entry.getValue());
                }
            }
        }
        if (userId != null) {
            claims.put("userId", userId);
        }
        if (role != null && !role.trim().isEmpty()) {
            claims.put("role", role.trim());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(subject.trim())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token.trim());
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("JWT token validation failed: token is expired.");
            return false;
        } catch (JwtException ex) {
            log.debug("JWT token validation failed: signature is invalid or token is malformed.");
            return false;
        } catch (Exception ex) {
            log.debug("JWT token validation failed: unexpected token error.");
            return false;
        }
    }

    @Override
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    @Override
    public Long extractUserId(String token) {
        Object userIdObj = extractClaims(token).get("userId");
        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    private Claims extractClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot extract claims from empty or null JWT token.");
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }
}
