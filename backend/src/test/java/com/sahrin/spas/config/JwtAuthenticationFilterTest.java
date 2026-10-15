package com.sahrin.spas.config;

import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String VALID_TOKEN = "valid.jwt.token";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userRepository);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("1. Valid Bearer JWT for ACTIVE user authenticates request and grants database role authority")
    void doFilter_validTokenActiveUser_authenticatesSuccessfully() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);

        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("student@university.edu");

        User activeStudent = new User(
                "Jane Doe",
                "STU-001",
                "student@university.edu",
                "$2a$10$encodedPasswordHash",
                Role.STUDENT,
                UserStatus.ACTIVE
        );
        activeStudent.setId(101L);

        when(userRepository.findById(101L)).thenReturn(Optional.of(activeStudent));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication must be set in SecurityContext");
        assertTrue(auth.isAuthenticated());
        assertEquals("student@university.edu", auth.getName());
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT")));

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("2. Invalid JWT signature/format does not authenticate the request")
    void doFilter_invalidToken_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalid.token");
        when(jwtService.validateToken("invalid.token")).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("3. Expired JWT throwing ExpiredJwtException does not authenticate the request")
    void doFilter_expiredJwt_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer expired.token");
        when(jwtService.validateToken("expired.token"))
                .thenThrow(new ExpiredJwtException(null, null, "JWT token expired"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("4. Missing Authorization header does not authenticate the request")
    void doFilter_missingAuthorizationHeader_doesNotAuthenticate() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("5. Non-Bearer Authorization header does not authenticate the request")
    void doFilter_nonBearerAuthorizationHeader_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("6a. Token with null user ID claim does not authenticate the request")
    void doFilter_missingUserIdClaim_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(null);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("student@university.edu");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("6b. Token with non-positive user ID claim does not authenticate the request")
    void doFilter_negativeUserIdClaim_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(-5L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("student@university.edu");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("6c. Token with blank subject claim does not authenticate the request")
    void doFilter_blankSubjectClaim_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("   ");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("7. User ID not found in database does not authenticate the request")
    void doFilter_userIdNotFoundInDatabase_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(999L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("ghost@university.edu");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("8. Token subject mismatch with database user email does not authenticate the request")
    void doFilter_subjectEmailMismatch_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("impostor@university.edu");

        User actualUser = new User(
                "Jane Doe",
                "STU-001",
                "actual.student@university.edu",
                "$2a$10$encodedPasswordHash",
                Role.STUDENT,
                UserStatus.ACTIVE
        );
        actualUser.setId(101L);
        when(userRepository.findById(101L)).thenReturn(Optional.of(actualUser));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("9. User with PENDING_APPROVAL status is not authenticated")
    void doFilter_pendingApprovalUser_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("pending@university.edu");

        User pendingUser = new User(
                "Pending Applicant",
                "STU-002",
                "pending@university.edu",
                "$2a$10$encodedPasswordHash",
                Role.STUDENT,
                UserStatus.PENDING_APPROVAL
        );
        pendingUser.setId(101L);
        when(userRepository.findById(101L)).thenReturn(Optional.of(pendingUser));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("10. User with REJECTED status is not authenticated")
    void doFilter_rejectedUser_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("rejected@university.edu");

        User rejectedUser = new User(
                "Rejected Applicant",
                "STU-003",
                "rejected@university.edu",
                "$2a$10$encodedPasswordHash",
                Role.STUDENT,
                UserStatus.REJECTED
        );
        rejectedUser.setId(101L);
        when(userRepository.findById(101L)).thenReturn(Optional.of(rejectedUser));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("11. User with null database role is safely rejected without granting authentication")
    void doFilter_nullDatabaseRole_doesNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("norole@university.edu");

        User userWithoutRole = new User(
                "No Role User",
                "STU-004",
                "norole@university.edu",
                "$2a$10$encodedPasswordHash",
                null,
                UserStatus.ACTIVE
        );
        userWithoutRole.setId(101L);
        when(userRepository.findById(101L)).thenReturn(Optional.of(userWithoutRole));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("12. Downstream filter chain is invoked exactly once regardless of JWT presence or validity")
    void doFilter_invokesFilterChainExactlyOnce() throws ServletException, IOException {
        // Sub-case A: Empty bearer token
        request.addHeader("Authorization", "Bearer ");
        jwtAuthenticationFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);

        // Sub-case B: Second request with invalid token
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        FilterChain filterChain2 = mock(FilterChain.class);
        request2.addHeader("Authorization", "Bearer bad-token");
        when(jwtService.validateToken("bad-token")).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request2, response2, filterChain2);
        verify(filterChain2, times(1)).doFilter(request2, response2);
        verifyNoMoreInteractions(filterChain2);
    }

    @Test
    @DisplayName("13. Authorities are derived strictly from database role and not from token role")
    void doFilter_derivesAuthoritiesStrictlyFromDatabaseRole() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("student@university.edu");

        // Database role is STUDENT (even if someone claimed ADMIN in token)
        User studentUser = new User(
                "Jane Doe",
                "STU-001",
                "student@university.edu",
                "$2a$10$encodedPasswordHash",
                Role.STUDENT,
                UserStatus.ACTIVE
        );
        studentUser.setId(101L);
        when(userRepository.findById(101L)).thenReturn(Optional.of(studentUser));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT")));
        assertFalse(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Verify that token role was never extracted or trusted
        verify(jwtService, never()).extractRole(any());
    }

    @Test
    @DisplayName("14. Downstream filter exceptions propagate directly and are never caught by the filter")
    void doFilter_downstreamException_propagatesUncaught() throws ServletException, IOException {
        doThrow(new ServletException("Downstream filter failed"))
                .when(filterChain).doFilter(request, response);

        ServletException thrown = assertThrows(
                ServletException.class,
                () -> jwtAuthenticationFilter.doFilter(request, response, filterChain)
        );

        assertEquals("Downstream filter failed", thrown.getMessage());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("15. Database failures during user lookup propagate directly and are not swallowed")
    void doFilter_databaseFailure_propagatesUncaught() {
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN)).thenReturn(101L);
        when(jwtService.extractSubject(VALID_TOKEN)).thenReturn("student@university.edu");

        when(userRepository.findById(101L))
                .thenThrow(new DataRetrievalFailureException("Database unreachable"));

        DataRetrievalFailureException thrown = assertThrows(
                DataRetrievalFailureException.class,
                () -> jwtAuthenticationFilter.doFilter(request, response, filterChain)
        );

        assertEquals("Database unreachable", thrown.getMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
