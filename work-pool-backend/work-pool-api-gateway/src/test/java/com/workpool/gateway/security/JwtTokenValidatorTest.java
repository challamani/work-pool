package com.workpool.gateway.security;

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

class JwtTokenValidatorTest {

    private static final String TEST_SECRET = "test_secret_key_for_unit_tests_only_x32";
    private JwtTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JwtTokenValidator(TEST_SECRET);
    }

    @Test
    void constructor_shortSecret_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenValidator("short"));
    }

    @Test
    void validateAndParseClaims_validToken_returnsClaims() {
        String token = buildToken("user-1", "test@example.com", "PUBLISHER");
        Claims claims = validator.validateAndParseClaims(token);
        assertNotNull(claims);
        assertEquals("user-1", claims.getSubject());
        assertEquals("test@example.com", claims.get("email", String.class));
    }

    @Test
    void validateAndParseClaims_invalidToken_throws() {
        assertThrows(Exception.class, () -> validator.validateAndParseClaims("not.a.token"));
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = buildToken("user-1", "a@b.com", "PUBLISHER");
        assertTrue(validator.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(validator.validateToken("bad.token.value"));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        String token = buildExpiredToken("user-1");
        assertFalse(validator.validateToken(token));
    }

    private String buildToken(String subject, String email, String roles) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .claim("roles", roles)
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
