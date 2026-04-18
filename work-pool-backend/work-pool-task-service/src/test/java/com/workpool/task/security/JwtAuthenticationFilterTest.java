package com.workpool.task.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_noToken_noAuthSet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_nonBearerHeader_noAuthSet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_invalidToken_noAuthSet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer badtoken");
        when(jwtTokenProvider.validateToken("badtoken")).thenReturn(false);
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_validTokenWithRoles_authSet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-1");
        when(claims.get("roles", String.class)).thenReturn("PUBLISHER");
        when(jwtTokenProvider.validateToken("validtoken")).thenReturn(true);
        when(jwtTokenProvider.validateAndParseClaims("validtoken")).thenReturn(claims);
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_validTokenNoRoles_authSet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken2");
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-2");
        when(claims.get("roles", String.class)).thenReturn(null);
        when(jwtTokenProvider.validateToken("validtoken2")).thenReturn(true);
        when(jwtTokenProvider.validateAndParseClaims("validtoken2")).thenReturn(claims);
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_claimsThrows_noAuthSet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer problematictoken");
        when(jwtTokenProvider.validateToken("problematictoken")).thenReturn(true);
        when(jwtTokenProvider.validateAndParseClaims(anyString())).thenThrow(
                new RuntimeException("error"));
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_validTokenBlankRoles_authSet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken3");
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-3");
        when(claims.get("roles", String.class)).thenReturn("  ");
        when(jwtTokenProvider.validateToken("validtoken3")).thenReturn(true);
        when(jwtTokenProvider.validateAndParseClaims("validtoken3")).thenReturn(claims);
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
