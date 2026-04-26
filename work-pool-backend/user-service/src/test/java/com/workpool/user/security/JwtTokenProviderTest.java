package com.workpool.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = "test_secret_key_for_unit_tests_only_x32";
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(TEST_SECRET, 86400000L);
    }

    @Test
    void constructor_shortSecret_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider("short", 86400000L));
    }

    @Test
    void generateToken_validInputs_returnsToken() {
        String token = provider.generateToken("user-1", "a@b.com", "PUBLISHER,FINISHER");
        assertNotNull(token);
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = provider.generateToken("user-1", "a@b.com", "PUBLISHER");
        assertTrue(provider.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(provider.validateToken("invalid.token"));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        String token = buildExpiredToken("user-1");
        assertFalse(provider.validateToken(token));
    }

    @Test
    void getUserIdFromToken_returnsSubject() {
        String token = provider.generateToken("user-42", "x@y.com", "PUBLISHER");
        assertEquals("user-42", provider.getUserIdFromToken(token));
    }

    @Test
    void getExpirationMs_returnsConfiguredValue() {
        assertEquals(86400000L, provider.getExpirationMs());
    }

    @Test
    void validateAndParseClaims_validToken_returnsClaims() {
        String token = provider.generateToken("user-1", "a@b.com", "PUBLISHER");
        Claims claims = provider.validateAndParseClaims(token);
        assertEquals("user-1", claims.getSubject());
        assertEquals("a@b.com", claims.get("email", String.class));
    }

    private String buildExpiredToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date past = new Date(System.currentTimeMillis() - 10000L);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(past)
                .expiration(past)
                .signWith(key)
                .compact();
    }
}
