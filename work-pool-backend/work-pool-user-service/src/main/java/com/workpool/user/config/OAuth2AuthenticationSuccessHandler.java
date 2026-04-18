package com.workpool.user.config;

import com.workpool.user.dto.AuthResponse;
import com.workpool.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    @Value("${app.oauth2.success-redirect-url:http://localhost:3000/auth/oauth2/callback}")
    private String successRedirectUrl;

    @Value("${app.oauth2.failure-redirect-url:http://localhost:3000/login}")
    private String failureRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendRedirect(failureRedirectUrl + "?error=invalid_oauth_authentication");
            return;
        }

        OAuth2User oAuth2User = token.getPrincipal();
        String provider = token.getAuthorizedClientRegistrationId();
        String providerId = resolveProviderId(provider, oAuth2User.getAttributes());
        String email = oAuth2User.getAttribute("email");
        String fullName = Optional.ofNullable(oAuth2User.<String>getAttribute("name"))
                .orElse(email);
        String profileImageUrl = resolveProfileImageUrl(provider, oAuth2User.getAttributes());

        if (providerId == null || providerId.isBlank()) {
            response.sendRedirect(failureRedirectUrl + "?error=missing_provider_id");
            return;
        }

        AuthResponse authResponse = userService.loginOrRegisterOAuth2(
                provider, providerId, email, fullName, profileImageUrl);

        String redirectUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                .queryParam("token", authResponse.getAccessToken())
                .build(true)
                .toUriString();

        clearAuthenticationAttributes(request);
        response.sendRedirect(redirectUrl);
    }

    private String resolveProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return toStringOrNull(attributes.get("sub"));
        }
        if ("facebook".equalsIgnoreCase(provider)) {
            return toStringOrNull(attributes.get("id"));
        }
        return null;
    }

    private String resolveProfileImageUrl(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return toStringOrNull(attributes.get("picture"));
        }
        if (!"facebook".equalsIgnoreCase(provider)) {
            return null;
        }

        Object picture = attributes.get("picture");
        if (!(picture instanceof Map<?, ?> pictureMap)) {
            return null;
        }
        Object data = pictureMap.get("data");
        if (!(data instanceof Map<?, ?> dataMap)) {
            return null;
        }
        return toStringOrNull(dataMap.get("url"));
    }

    private String toStringOrNull(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
