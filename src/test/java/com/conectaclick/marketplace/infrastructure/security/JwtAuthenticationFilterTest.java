package com.conectaclick.marketplace.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @MethodSource("invalidAuthorizationHeaders")
    void shouldDoFilterWhenAuthorizationHeaderIsInvalid(String authorizationHeader) throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil, never()).getUsernameFromToken(any());
        verify(jwtTokenUtil, never()).validateToken(any());
    }

    static Stream<String> invalidAuthorizationHeaders() {
        return Stream.of(null, "Basic token123", "bearer token123");
    }

    @Test
    void shouldDoFilterWhenTokenIsInvalid() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenUtil.getUsernameFromToken(invalidToken))
                .thenThrow(new RuntimeException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(invalidToken);
        verify(jwtTokenUtil, never()).validateToken(any());
    }

    @Test
    void shouldAuthenticateWhenTokenIsValid() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String username = "testuser";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenUtil.getUsernameFromToken(validToken)).thenReturn(username);
        when(jwtTokenUtil.validateToken(validToken)).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(validToken);
        verify(jwtTokenUtil).validateToken(validToken);
        
        // Verify authentication is set in context
        assert SecurityContextHolder.getContext().getAuthentication() != null;
        assert SecurityContextHolder.getContext().getAuthentication().getName().equals(username);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsNotValid() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        String username = "testuser";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenUtil.getUsernameFromToken(invalidToken)).thenReturn(username);
        when(jwtTokenUtil.validateToken(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(invalidToken);
        verify(jwtTokenUtil).validateToken(invalidToken);
        
        // Verify authentication is NOT set in context
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    void shouldNotAuthenticateWhenUserAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String username = "testuser";
        
        // Pre-authenticate user
        UsernamePasswordAuthenticationToken existingAuth = 
                new UsernamePasswordAuthenticationToken("existinguser", null, null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenUtil.getUsernameFromToken(validToken)).thenReturn(username);
        // validateToken is not called when user is already authenticated

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(validToken);
        verify(jwtTokenUtil, never()).validateToken(validToken);
        
        // Verify existing authentication is preserved
        assert SecurityContextHolder.getContext().getAuthentication() == existingAuth;
        assert SecurityContextHolder.getContext().getAuthentication().getName().equals("existinguser");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer ", "Bearer null", "Bearer    token123"})
    void shouldHandleInvalidTokenFormats(String authorizationHeader) throws ServletException, IOException {
        // Given
        String expectedToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtTokenUtil.getUsernameFromToken(expectedToken)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(expectedToken);
        verify(jwtTokenUtil, never()).validateToken(any());
    }

    @Test
    void shouldHandleExceptionInGetUsernameFromToken() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenUtil.getUsernameFromToken(invalidToken))
                .thenThrow(new RuntimeException("Token parsing error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(invalidToken);
        verify(jwtTokenUtil, never()).validateToken(any());
    }

    @Test
    void shouldHandleExpiredToken() throws ServletException, IOException {
        // Given
        String expiredToken = "expired.jwt.token";
        String username = "testuser";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(jwtTokenUtil.getUsernameFromToken(expiredToken)).thenReturn(username);
        when(jwtTokenUtil.validateToken(expiredToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(expiredToken);
        verify(jwtTokenUtil).validateToken(expiredToken);
        
        // Verify authentication is NOT set in context
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    void shouldHandleMalformedToken() throws ServletException, IOException {
        // Given
        String malformedToken = "not.a.valid.jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + malformedToken);
        when(jwtTokenUtil.getUsernameFromToken(malformedToken)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(malformedToken);
        verify(jwtTokenUtil, never()).validateToken(any());
        
        // Verify authentication is NOT set in context
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    void shouldHandleTokenWithSpecialCharacters() throws ServletException, IOException {
        // Given
        String tokenWithSpecialChars = "token.with.special.chars.123";
        String username = "specialuser";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithSpecialChars);
        when(jwtTokenUtil.getUsernameFromToken(tokenWithSpecialChars)).thenReturn(username);
        when(jwtTokenUtil.validateToken(tokenWithSpecialChars)).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(tokenWithSpecialChars);
        verify(jwtTokenUtil).validateToken(tokenWithSpecialChars);
        
        // Verify authentication is set in context
        assert SecurityContextHolder.getContext().getAuthentication() != null;
        assert SecurityContextHolder.getContext().getAuthentication().getName().equals(username);
    }

    @Test
    void shouldHandleVeryLongToken() throws ServletException, IOException {
        // Given
        String longToken = "very.long.jwt.token.with.many.parts.that.exceeds.normal.length.123456789";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + longToken);
        when(jwtTokenUtil.getUsernameFromToken(longToken)).thenReturn(null); // Return null to avoid validateToken call

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(longToken);
        verify(jwtTokenUtil, never()).validateToken(any());
    }

    @Test
    void shouldHandleTokenWithNumbersOnly() throws ServletException, IOException {
        // Given
        String numericToken = "123456789";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + numericToken);
        when(jwtTokenUtil.getUsernameFromToken(numericToken)).thenReturn(null); // Return null to avoid validateToken call

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(numericToken);
        verify(jwtTokenUtil, never()).validateToken(any());
    }

    @Test
    void shouldHandleMultipleBearerPrefixes() throws ServletException, IOException {
        // Given
        String token = "Bearer Bearer token123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.getUsernameFromToken(token)).thenReturn(null); // Return null to avoid validateToken call

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(token);
        verify(jwtTokenUtil, never()).validateToken(any());
    }

    
    
    @Test
    void shouldSetAuthenticationWithCorrectAuthorities() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String username = "testuser";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenUtil.getUsernameFromToken(validToken)).thenReturn(username);
        when(jwtTokenUtil.validateToken(validToken)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(username, auth.getName());
        assertTrue(auth.getAuthorities().isEmpty()); // User has no authorities
        assertTrue(auth.isAuthenticated());
    }

    @Test
    void shouldPreserveExistingAuthenticationWhenTokenInvalid() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        String username = "testuser";
        
        // Pre-authenticate user
        UsernamePasswordAuthenticationToken existingAuth = 
                new UsernamePasswordAuthenticationToken("existinguser", null, null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenUtil.getUsernameFromToken(invalidToken)).thenReturn(username);
        // validateToken is not called when user is already authenticated

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenUtil).getUsernameFromToken(invalidToken);
        verify(jwtTokenUtil, never()).validateToken(invalidToken);
        
        // Verify existing authentication is preserved
        assert SecurityContextHolder.getContext().getAuthentication() == existingAuth;
        assert SecurityContextHolder.getContext().getAuthentication().getName().equals("existinguser");
    }
}
