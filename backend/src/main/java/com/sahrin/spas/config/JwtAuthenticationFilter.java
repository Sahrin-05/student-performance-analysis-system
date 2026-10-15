package com.sahrin.spas.config;

import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filter that intercepts incoming HTTP requests to validate JWT Bearer tokens
 * and authenticate the user in the Spring SecurityContext if valid and active.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        authenticateRequestIfValidJwt(request);

        filterChain.doFilter(request, response);
    }

    /**
     * Inspects the Authorization header for a Bearer JWT, validates it, and if valid,
     * populates the SecurityContext with an authenticated principal derived from the database.
     *
     * <p>Exceptions during token parsing and claim extraction are caught and logged at debug level,
     * leaving the request unauthenticated. Database or unrelated runtime failures are not swallowed.
     */
    private void authenticateRequestIfValidJwt(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 1. Process header only if it starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return;
        }

        final String jwtToken = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (jwtToken.isEmpty()) {
            return;
        }

        Long userId;
        String tokenSubject;

        // 2. Narrow exception handling to expected JWT parsing/claim-extraction failures
        try {
            if (!jwtService.validateToken(jwtToken)) {
                log.debug("JWT validation failed for incoming request.");
                return;
            }

            userId = jwtService.extractUserId(jwtToken);
            tokenSubject = jwtService.extractSubject(jwtToken);
        } catch (JwtException | IllegalArgumentException | ClassCastException ex) {
            log.debug("JWT parsing or claim extraction failed.");
            return;
        }

        // 3. Reject invalid, null, or incomplete token claims
        if (userId == null || userId <= 0 || tokenSubject == null || tokenSubject.trim().isEmpty()) {
            log.debug("JWT authentication rejected: missing or invalid userId or subject claim.");
            return;
        }

        // 4. If SecurityContext is not already authenticated, verify against database
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isEmpty()) {
                log.debug("JWT authentication rejected: user does not exist in database.");
                return;
            }

            User user = userOptional.get();

            // 5. Verify database user email matches token subject (case-insensitive)
            if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(tokenSubject.trim())) {
                log.debug("JWT authentication rejected: token subject does not match database user email.");
                return;
            }

            // 6. Verify user status is ACTIVE
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.debug("JWT authentication rejected: user status is not ACTIVE (current status: {}).", user.getStatus());
                return;
            }

            // 7. Verify user role exists in database - safely handle missing role
            if (user.getRole() == null) {
                log.debug("JWT authentication rejected: user has no assigned role in database.");
                return;
            }

            // 8. Build authorities using current DB role (ROLE_ prefix) - do NOT trust token role
            String authorityName = "ROLE_" + user.getRole().name();
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authorityName));

            // 9. Create authenticated token with user email as principal
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    authorities
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 10. Store authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
