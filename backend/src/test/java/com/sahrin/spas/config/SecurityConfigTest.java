package com.sahrin.spas.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {SecurityConfig.class, SecurityConfigTest.TestEndpointController.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RestController
    static class TestEndpointController {
        @GetMapping("/api/auth/test")
        public ResponseEntity<String> authEndpoint() {
            return ResponseEntity.ok("Public Auth Endpoint");
        }

        @GetMapping("/api/protected/test")
        public ResponseEntity<String> protectedEndpoint() {
            return ResponseEntity.ok("Protected Endpoint");
        }
    }

    @Test
    @DisplayName("PasswordEncoder bean should encode and verify passwords using BCrypt")
    void testPasswordEncoder() {
        assertNotNull(passwordEncoder, "PasswordEncoder bean should not be null");

        String rawPassword = "SecurePassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword, "Password should be hashed, not plaintext");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "Correct password should match the hash");
        assertFalse(passwordEncoder.matches("WrongPassword", encodedPassword), "Incorrect password should not match");
    }

    @Test
    @DisplayName("Endpoints under /api/auth/** should be accessible without authentication")
    void testAuthEndpointsArePermitted() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Non-auth endpoints should be rejected when unauthenticated")
    void testProtectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CORS preflight from frontend origin http://localhost:5173 should be allowed")
    void testCorsConfiguration_port5173_allowed() throws Exception {
        mockMvc.perform(options("/api/auth/test")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("CORS preflight from frontend origin http://localhost:5174 should be allowed")
    void testCorsConfiguration_port5174_allowed() throws Exception {
        mockMvc.perform(options("/api/auth/test")
                        .header("Origin", "http://localhost:5174")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
    }

    @Test
    @DisplayName("CORS preflight from unpermitted origin should be forbidden")
    void testCorsConfiguration_unpermittedOrigin_forbidden() throws Exception {
        mockMvc.perform(options("/api/auth/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
