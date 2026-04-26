package com.workpool.user.service;

import com.workpool.user.model.LoginAudit;
import com.workpool.user.repository.LoginAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAuditServiceTest {

    @Mock
    private LoginAuditRepository loginAuditRepository;

    @InjectMocks
    private LoginAuditService loginAuditService;

    @Test
    void record_allHeadersPresent_savesWithForwardedIp() {
        when(loginAuditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");
        request.addHeader("User-Agent", "Mozilla");
        request.addHeader("Accept-Language", "en-US");
        request.addHeader("Origin", "https://example.com");
        request.addHeader("Referer", "https://example.com/page");
        request.addHeader("X-Request-Id", "req-abc");
        request.addHeader("CF-IPCountry", "IN");
        request.addHeader("X-AppEngine-City", "Mumbai");

        loginAuditService.record("user-1", "a@b.com", "PASSWORD", true, "SUCCESS", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());
        LoginAudit audit = captor.getValue();
        assertEquals("1.2.3.4", audit.getClientIp());
        assertEquals("1.2.3.4, 5.6.7.8", audit.getForwardedFor());
        assertEquals("Mozilla", audit.getUserAgent());
        assertEquals("req-abc", audit.getRequestId());
        assertEquals("IN", audit.getGeoCountry());
        assertEquals("Mumbai", audit.getGeoCity());
    }

    @Test
    void record_xRealIpHeader_usesRealIp() {
        when(loginAuditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "9.9.9.9");

        loginAuditService.record("user-1", "a@b.com", "PASSWORD", true, "SUCCESS", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());
        assertEquals("9.9.9.9", captor.getValue().getClientIp());
    }

    @Test
    void record_noForwardingHeaders_usesRemoteAddr() {
        when(loginAuditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        loginAuditService.record(null, "a@b.com", "PASSWORD", false, "USER_NOT_FOUND", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());
        assertEquals("127.0.0.1", captor.getValue().getClientIp());
    }

    @Test
    void record_blankHeaders_treatedAsNull() {
        when(loginAuditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "   ");
        request.addHeader("X-Real-IP", "  ");
        request.setRemoteAddr("10.0.0.1");

        loginAuditService.record("u1", "x@y.com", "PASSWORD", false, "FAILED", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());
        assertEquals("10.0.0.1", captor.getValue().getClientIp());
        assertNull(captor.getValue().getForwardedFor());
    }

    @Test
    void record_correlationIdFallback_usesCorrelationId() {
        when(loginAuditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "corr-999");

        loginAuditService.record("u1", "x@y.com", "PASSWORD", true, "SUCCESS", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());
        assertEquals("corr-999", captor.getValue().getRequestId());
    }

    @Test
    void record_geoCityFallback_usesXGeoCity() {
        when(loginAuditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Geo-City", "Delhi");

        loginAuditService.record("u1", "x@y.com", "PASSWORD", true, "SUCCESS", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());
        assertEquals("Delhi", captor.getValue().getGeoCity());
    }

    @Test
    void record_countryFallback_usesXCountryCode() {
        when(loginAuditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Country-Code", "US");

        loginAuditService.record("u1", "x@y.com", "PASSWORD", true, "SUCCESS", request);

        ArgumentCaptor<LoginAudit> captor = ArgumentCaptor.forClass(LoginAudit.class);
        verify(loginAuditRepository).save(captor.capture());
        assertEquals("US", captor.getValue().getGeoCountry());
    }
}
