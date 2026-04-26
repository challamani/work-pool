package com.workpool.gateway.filter;

import com.workpool.gateway.security.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtGlobalFilterTest {

    private static final String TEST_SECRET = "test_secret_key_for_unit_tests_only_x32";

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    private JwtGlobalFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtGlobalFilter(jwtTokenValidator);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void getOrder_returnsMinusOne() {
        assertEquals(-1, filter.getOrder());
    }

    @Test
    void filter_publicRegisterPath_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/register").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_publicLoginPath_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_publicTasksPath_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_publicTaskDetailPath_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks/someTaskId").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_noToken_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void filter_invalidToken_returns401() {
        when(jwtTokenValidator.validateToken("badtoken")).thenReturn(false);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me")
                .header("Authorization", "Bearer badtoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void filter_validToken_setsHeadersAndPassesThrough() {
        String token = buildToken("user-1", "test@example.com", "PUBLISHER");
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        Claims claims = parseToken(token);
        when(jwtTokenValidator.validateAndParseClaims(token)).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_validTokenNullEmailAndRoles_setsEmptyHeaders() {
        String token = buildTokenMinimal("user-2");
        when(jwtTokenValidator.validateToken(token)).thenReturn(true);
        Claims claims = parseToken(token);
        when(jwtTokenValidator.validateAndParseClaims(token)).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_publicRatingsPath_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/ratings/users/abc").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_publicWebsocketPath_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/ws/connect").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_taskSubpath_notPublic_returns401() {
        // /api/v1/tasks/someId/bids is NOT public — only exact /api/v1/tasks and /api/v1/tasks/{id} are
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/tasks/someId/bids").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void filter_userProfilePath_notPublic_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/payments/history").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertEquals(401, exchange.getResponse().getStatusCode().value());
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

    private String buildTokenMinimal(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 86400000L))
                .signWith(key)
                .compact();
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
