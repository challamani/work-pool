package com.workpool.notification.security;

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
        provider = new JwtTokenProvider(TEST_SECRET);
    }

    @Test
    void constructor_shortSecret_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider("short"));
    }

    @Test
    void validateAndParseClaims_validToken_returnsClaims() {
        String token = buildToken("user-1");
        Claims claims = provider.validateAndParseClaims(token);
        assertNotNull(claims);
        assertEquals("user-1", claims.getSubject());
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = buildToken("user-1");
        assertTrue(provider.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(provider.validateToken("bad.token"));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        String token = buildExpiredToken("user-1");
        assertFalse(provider.validateToken(token));
    }

    private String buildToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 86400000L))
                .signWith(key)
                .compact();
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
