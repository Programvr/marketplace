package com.conectaclick.marketplace.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private String testSecret = "testSecretKeyThatIsLongEnoughForHS256Algorithm123456789";
    private Long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", testExpiration);
    }

    @Test
    void shouldGenerateValidToken() {
        // Given
        String username = "testuser";

        // When
        String token = jwtTokenUtil.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsernameFromValidToken() {
        // Given
        String username = "testuser";
        String token = jwtTokenUtil.generateToken(username);

        // When
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        String username = "testuser";
        String token = jwtTokenUtil.generateToken(username);

        // When
        boolean isValid = jwtTokenUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldNotValidateExpiredToken() throws InterruptedException {
        // Given
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 1L); // 1 millisecond
        String username = "testuser";
        String token = jwtTokenUtil.generateToken(username);
        
        // Wait for token to expire
        Thread.sleep(10);

        // When
        boolean isValid = jwtTokenUtil.validateToken(token);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        // Given
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 1L); // 1 millisecond
        String username = "testuser";
        String token = jwtTokenUtil.generateToken(username);
        
        // Wait for token to expire
        Thread.sleep(10);

        // When
        boolean isExpired = jwtTokenUtil.isTokenExpired(token);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void shouldNotDetectNonExpiredToken() {
        // Given
        String username = "testuser";
        String token = jwtTokenUtil.generateToken(username);

        // When
        boolean isExpired = jwtTokenUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void shouldNotValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtTokenUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldDetectExpiredTokenForInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isExpired = jwtTokenUtil.isTokenExpired(invalidToken);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void shouldThrowExceptionForInvalidTokenWhenExtractingUsername() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThrows(JwtException.class, () -> {
            jwtTokenUtil.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void shouldGenerateDifferentTokensForSameUser() throws InterruptedException {
        // Given
        String username = "testuser";

        // When - generate tokens with a delay between them
        String token1 = jwtTokenUtil.generateToken(username);
        Thread.sleep(100); // Longer delay to ensure different timestamps
        String token2 = jwtTokenUtil.generateToken(username);

        // Then - tokens should be different due to different iat (issued at) claims
        assertNotEquals("Tokens should be different due to different timestamps", token1, token2);
        
        // Additionally verify that both tokens are valid and contain the same username
        assertEquals(username, jwtTokenUtil.getUsernameFromToken(token1));
        assertEquals(username, jwtTokenUtil.getUsernameFromToken(token2));
        assertTrue(jwtTokenUtil.validateToken(token1));
        assertTrue(jwtTokenUtil.validateToken(token2));
    }

    @Test
    void shouldExtractCorrectUsernameFromMultipleTokens() {
        // Given
        String username1 = "user1";
        String username2 = "user2";
        String token1 = jwtTokenUtil.generateToken(username1);
        String token2 = jwtTokenUtil.generateToken(username2);

        // When
        String extractedUsername1 = jwtTokenUtil.getUsernameFromToken(token1);
        String extractedUsername2 = jwtTokenUtil.getUsernameFromToken(token2);

        // Then
        assertEquals(username1, extractedUsername1);
        assertEquals(username2, extractedUsername2);
    }

    @Test
    void shouldValidateMultipleValidTokens() {
        // Given
        String username = "testuser";
        String token1 = jwtTokenUtil.generateToken(username);
        String token2 = jwtTokenUtil.generateToken(username);

        // When
        boolean isValid1 = jwtTokenUtil.validateToken(token1);
        boolean isValid2 = jwtTokenUtil.validateToken(token2);

        // Then
        assertTrue(isValid1);
        assertTrue(isValid2);
    }

    @Test
    void shouldHandleNullToken() {
        // When
        boolean isValid = jwtTokenUtil.validateToken(null);
        boolean isExpired = jwtTokenUtil.isTokenExpired(null);

        // Then
        assertFalse(isValid);
        assertTrue(isExpired);
    }

    @Test
    void shouldHandleEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenUtil.validateToken(emptyToken);
        boolean isExpired = jwtTokenUtil.isTokenExpired(emptyToken);

        // Then
        assertFalse(isValid);
        assertTrue(isExpired);
    }

    @Test
    void shouldHandleTokenWithSpecialCharacters() {
        // Given
        String username = "user@special#$%";

        // When
        String token = jwtTokenUtil.generateToken(username);
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        boolean isValid = jwtTokenUtil.validateToken(token);

        // Then
        assertEquals(username, extractedUsername);
        assertTrue(isValid);
    }

    @Test
    void shouldHandleLongUsername() {
        // Given
        String longUsername = "verylongusernamewithlotsofcharacterstotesttokenhandling1234567890";

        // When
        String token = jwtTokenUtil.generateToken(longUsername);
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        boolean isValid = jwtTokenUtil.validateToken(token);

        // Then
        assertEquals(longUsername, extractedUsername);
        assertTrue(isValid);
    }

    @Test
    void shouldCreateSigningKeyCorrectly() {
        // Given
        String secret = "testSecretKeyThatIsLongEnoughForHS256Algorithm123456789";
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", secret);

        // When
        String username = "testuser";
        String token = jwtTokenUtil.generateToken(username);

        // Then
        assertNotNull(token);
        assertEquals(username, jwtTokenUtil.getUsernameFromToken(token));
        assertTrue(jwtTokenUtil.validateToken(token));
    }

    @Test
    void shouldValidateTokenWithDifferentSecret() {
        // Given
        String username = "testuser";
        String token = jwtTokenUtil.generateToken(username);
        
        // Change the secret
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", "differentSecret12345678901234567890");

        // When
        boolean isValid = jwtTokenUtil.validateToken(token);

        // Then
        assertFalse(isValid);
    }
}
