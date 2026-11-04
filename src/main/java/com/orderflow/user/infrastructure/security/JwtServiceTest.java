package com.orderflow.user.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        byte[] secret = new byte[32];

        for (int i = 0; i < secret.length; i++) {
            secret[i] = (byte) (i + 1);
        }

        String encodedSecret = Base64.getEncoder()
                .encodeToString(secret);

        jwtService = new JwtService(encodedSecret, 3600000);
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken("test@example.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateToken("test@example.com");

        String email = jwtService.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.value"));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken("test@example.com");

        String tamperedToken = token + "invalid";

        assertFalse(jwtService.isTokenValid(tamperedToken));
    }
}