package com.workpool.user.service;

import com.workpool.user.model.LoginAudit;
import com.workpool.user.repository.LoginAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAuditService {

    private final LoginAuditRepository loginAuditRepository;

    public void record(String userId, String email, String authProvider, boolean success,
                       String reason, HttpServletRequest request) {
        loginAuditRepository.save(LoginAudit.builder()
                .userId(userId)
                .email(email)
                .authProvider(authProvider)
                .success(success)
                .reason(reason)
                .clientIp(extractClientIp(request))
                .forwardedFor(header(request, "X-Forwarded-For"))
                .userAgent(header(request, "User-Agent"))
                .acceptLanguage(header(request, "Accept-Language"))
                .origin(header(request, "Origin"))
                .referer(header(request, "Referer"))
                .requestId(firstPresent(request, "X-Request-Id", "X-Correlation-Id"))
                .geoCountry(firstPresent(request, "CF-IPCountry", "X-Country-Code"))
                .geoCity(firstPresent(request, "X-AppEngine-City", "X-Geo-City"))
                .build());
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = header(request, "X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = header(request, "X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String firstPresent(HttpServletRequest request, String... keys) {
        for (String key : keys) {
            String value = request.getHeader(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String header(HttpServletRequest request, String key) {
        String value = request.getHeader(key);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
