package com.sahrin.spas.service;

import com.sahrin.spas.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    // Deterministic 32-byte test-only secret key (256 bits for HMAC-SHA256)
    private static final String TEST_SECRET = "TestDeterministicSecretKeyForJwt256BitsLong!";
    private static final String DIFFERENT_SECRET = "AnotherDeterministicKeyThatIsAtLeast32Bytes!";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(TEST_SECRET, TEST_EXPIRATION_MS);
    }

    @Test
    @DisplayName("Should successfully generate a valid token and extract claims")
    void generateToken_andExtractClaims_success() {
        String token = jwtService.generateToken(42L, "admin@university.edu", "ADMIN");

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertTrue(jwtService.validateToken(token));
        assertEquals("admin@university.edu", jwtService.extractSubject(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertFalse(jwtService.isTokenExpired(token));

        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should successfully generate and validate token without userId")
    void generateToken_withoutUserId_success() {
        String token = jwtService.generateToken("student@university.edu", "STUDENT");

        assertTrue(jwtService.validateToken(token));
        assertEquals("student@university.edu", jwtService.extractSubject(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
        assertNull(jwtService.extractUserId(token));
    }

    @Test
    @DisplayName("Should support extra custom claims")
    void generateToken_withExtraClaims_success() {
        Map<String, Object> extra = Map.of("department", "Computer Science", "status", "ACTIVE");
        String token = jwtService.generateToken(10L, "lecturer@university.edu", "LECTURER", extra);

        assertTrue(jwtService.validateToken(token));
        assertEquals("lecturer@university.edu", jwtService.extractSubject(token));
        assertEquals("LECTURER", jwtService.extractRole(token));
        assertEquals(10L, jwtService.extractUserId(token));
    }

    @Test
    @DisplayName("Should fail validation when token is signed with a different key")
    void validateToken_withDifferentSigningKey_returnsFalse() {
        JwtServiceImpl anotherService = new JwtServiceImpl(DIFFERENT_SECRET, TEST_EXPIRATION_MS);
        String tokenFromAnotherService = anotherService.generateToken(1L, "user@university.edu", "STUDENT");

        // Token signed by another secret must fail validation
        assertFalse(jwtService.validateToken(tokenFromAnotherService));
    }

    @Test
    @DisplayName("Should fail validation and identify expired token")
    void validateToken_withExpiredToken_returnsFalseAndIsExpired() {
        // Manually construct an expired token using the exact same secret key
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        String expiredToken = Jwts.builder()
                .subject("expired@university.edu")
                .claim("role", "STUDENT")
                .issuedAt(new Date(now - 100000))
                .expiration(new Date(now - 50000)) // Expired in the past
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtService.validateToken(expiredToken));
        assertTrue(jwtService.isTokenExpired(expiredToken));
    }

    @Test
    @DisplayName("Should fail validation on malformed or manipulated token strings")
    void validateToken_withMalformedToken_returnsFalse() {
        assertFalse(jwtService.validateToken("header.payload.invalidsignature"));
        assertFalse(jwtService.validateToken("random-string-not-a-token"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken("   "));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when secret is missing or empty")
    void constructor_missingSecret_throwsIllegalStateException() {
        IllegalStateException ex1 = assertThrows(
                IllegalStateException.class,
                () -> new JwtServiceImpl(null, TEST_EXPIRATION_MS)
        );
        assertTrue(ex1.getMessage().contains("Secret key is not configured"));

        IllegalStateException ex2 = assertThrows(
                IllegalStateException.class,
                () -> new JwtServiceImpl("   ", TEST_EXPIRATION_MS)
        );
        assertTrue(ex2.getMessage().contains("Secret key is not configured"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when secret is shorter than 256 bits (32 bytes)")
    void constructor_shortSecret_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtServiceImpl("short-secret-only-24-bytes!", TEST_EXPIRATION_MS)
        );
        assertTrue(ex.getMessage().contains("must be at least 256 bits (32 bytes)"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when expiration is zero or negative")
    void constructor_invalidExpiration_throwsIllegalArgumentException() {
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtServiceImpl(TEST_SECRET, 0)
        );
        assertTrue(ex1.getMessage().contains("must be greater than zero"));

        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtServiceImpl(TEST_SECRET, -1000)
        );
        assertTrue(ex2.getMessage().contains("must be greater than zero"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when token subject is empty or null")
    void generateToken_invalidSubject_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generateToken(null, "ADMIN")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generateToken("   ", "ADMIN")
        );
    }

    @Test
    @DisplayName("Should prevent extraClaims from overriding dedicated reserved claims (role, userId, sub, iat, exp)")
    void generateToken_withReservedClaimsInExtraClaims_doesNotOverrideDedicatedParameters() {
        Map<String, Object> maliciousClaims = Map.of(
                "role", "ADMIN",
                "ROLE", "SUPER_ADMIN",
                "userId", 9999L,
                "USERID", 8888L,
                "sub", "hacked@university.edu",
                "iat", 0L,
                "exp", System.currentTimeMillis() + 999999999L,
                "department", "Engineering"
        );

        String token = jwtService.generateToken(15L, "legit.student@university.edu", "STUDENT", maliciousClaims);

        assertTrue(jwtService.validateToken(token));
        assertEquals("legit.student@university.edu", jwtService.extractSubject(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
        assertEquals(15L, jwtService.extractUserId(token));

        Date expiration = jwtService.extractExpiration(token);
        long diffMs = expiration.getTime() - System.currentTimeMillis();
        assertTrue(diffMs <= TEST_EXPIRATION_MS && diffMs > 0);
    }

    @Test
    @DisplayName("Should prevent extraClaims from injecting unauthorized role when role parameter is null or blank")
    void generateToken_withNullOrBlankRole_cannotInjectRoleThroughExtraClaims() {
        Map<String, Object> maliciousClaims = Map.of(
                "role", "ADMIN",
                "Role", "ADMINISTRATOR",
                "ROLE", "SUPER_ADMIN"
        );

        // When role parameter is null
        String token1 = jwtService.generateToken(20L, "user@university.edu", null, maliciousClaims);
        assertTrue(jwtService.validateToken(token1));
        assertNull(jwtService.extractRole(token1));

        // When role parameter is blank
        String token2 = jwtService.generateToken(20L, "user@university.edu", "   ", maliciousClaims);
        assertTrue(jwtService.validateToken(token2));
        assertNull(jwtService.extractRole(token2));
    }

    @Test
    @DisplayName("Should prevent extraClaims from injecting unauthorized userId when userId parameter is null")
    void generateToken_withNullUserId_cannotInjectUserIdThroughExtraClaims() {
        Map<String, Object> maliciousClaims = Map.of(
                "userId", 999L,
                "USERID", 888L,
                "userid", 777L
        );

        String token = jwtService.generateToken(null, "user@university.edu", "STUDENT", maliciousClaims);
        assertTrue(jwtService.validateToken(token));
        assertNull(jwtService.extractUserId(token));
    }
}
