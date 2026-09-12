package com.filehider.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "TestJwtSecretKeyThatMustBeAtLeast256BitsLongForHMACSHA256!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Should generate valid token with correct email and name claims")
    void testGenerateAndValidateToken() {
        String email = "satwik@cyphervault.io";
        String name = "Satwik Saxena";

        String token = jwtService.generateToken(email, name);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(name, jwtService.extractName(token));
    }

    @Test
    @DisplayName("Should reject modified or invalid JWT tokens")
    void testInvalidToken() {
        String token = jwtService.generateToken("test@example.com", "Tester");
        String tampered = token + "xyz";

        assertFalse(jwtService.validateToken(tampered));
        assertNull(jwtService.extractEmail(tampered));
    }

    @Test
    @DisplayName("Should handle expired tokens correctly")
    void testExpiredToken() {
        // Expiration of -1000ms (already expired)
        JwtService expiredService = new JwtService(TEST_SECRET, -1000L);
        String token = expiredService.generateToken("user@example.com", "User");

        assertFalse(expiredService.validateToken(token));
    }
}
