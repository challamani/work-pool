package com.workpool.user.service;

import com.workpool.common.enums.UserRole;
import com.workpool.common.exception.ResourceNotFoundException;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.common.model.GeoLocation;
import com.workpool.user.dto.*;
import com.workpool.user.model.User;
import com.workpool.user.repository.UserRepository;
import com.workpool.user.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final LoginAuditService loginAuditService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration ACCOUNT_LOCK_DURATION = Duration.ofMinutes(15);

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new WorkPoolException("EMAIL_TAKEN", "Email is already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(UserRole.PUBLISHER, UserRole.FINISHER))
                .build();
        user = userRepository.save(user);
        log.info("Registered new user: {}", user.getId());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        String userId = user != null ? user.getId() : null;
        try {
            if (user != null && isLocked(user)) {
                loginAuditService.record(userId, request.getEmail(), "PASSWORD", false,
                        "ACCOUNT_LOCKED", httpServletRequest);
                throw new WorkPoolException("ACCOUNT_LOCKED", "Account is temporarily locked due to failed login attempts");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            if (user == null) {
                throw new ResourceNotFoundException("User", request.getEmail());
            }

            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
            loginAuditService.record(user.getId(), user.getEmail(), "PASSWORD", true, "SUCCESS", httpServletRequest);
            return buildAuthResponse(user);
        } catch (AuthenticationException ex) {
            if (user != null) {
                registerFailure(user);
                loginAuditService.record(user.getId(), user.getEmail(), "PASSWORD", false,
                        "INVALID_CREDENTIALS", httpServletRequest);
            } else {
                loginAuditService.record(null, request.getEmail(), "PASSWORD", false,
                        "USER_NOT_FOUND", httpServletRequest);
            }
            throw new WorkPoolException("UNAUTHORIZED", "Invalid email or password");
        }
    }

    public AuthResponse loginOrRegisterOAuth2(String provider, String providerId,
                                               String email, String fullName, String profileImageUrl) {
        User user = userRepository.findByOauthProviderAndOauthProviderId(provider, providerId)
                .orElseGet(() -> {
                    if (email != null && userRepository.existsByEmail(email)) {
                        User existing = userRepository.findByEmail(email).get();
                        existing.setOauthProvider(provider);
                        existing.setOauthProviderId(providerId);
                        existing.setEmailVerified(true);
                        return userRepository.save(existing);
                    }
                    User newUser = User.builder()
                            .email(email)
                            .fullName(fullName)
                            .profileImageUrl(profileImageUrl)
                            .oauthProvider(provider)
                            .oauthProviderId(providerId)
                            .emailVerified(true)
                            .roles(Set.of(UserRole.PUBLISHER, UserRole.FINISHER))
                            .build();
                    return userRepository.save(newUser);
                });
        return buildAuthResponse(user);
    }

    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return toProfileResponse(user);
    }

    public UserProfileResponse getPublicProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        UserProfileResponse profile = toProfileResponse(user);
        profile.setEmail(null);
        profile.setPhoneNumber(null);
        profile.setLocation(sanitizeLocation(user.getLocation()));
        return profile;
    }

    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) {
            if (!request.getPhoneNumber().equals(user.getPhoneNumber())
                    && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new WorkPoolException("PHONE_TAKEN", "Phone number already in use");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getServiceRadiusKm() != null) user.setServiceRadiusKm(request.getServiceRadiusKm());
        if (request.getBio() != null) user.setBio(request.getBio());

        user = userRepository.save(user);
        return toProfileResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), roles);
        return AuthResponse.of(token, jwtTokenProvider.getExpirationMs() / 1000, toProfileResponse(user));
    }

    public UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .roles(user.getRoles())
                .skills(user.getSkills())
                .location(user.getLocation())
                .serviceRadiusKm(user.getServiceRadiusKm())
                .bio(user.getBio())
                .aadhaarVerification(user.getAadhaarVerification())
                .averageRating(user.getAverageRating())
                .totalRatings(user.getTotalRatings())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private boolean isLocked(User user) {
        return user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(Instant.now());
    }

    private void registerFailure(User user) {
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(Instant.now().plus(ACCOUNT_LOCK_DURATION));
            user.setFailedLoginAttempts(0);
        }
        userRepository.save(user);
    }

    private GeoLocation sanitizeLocation(GeoLocation location) {
        if (location == null) {
            return null;
        }
        return new GeoLocation(0, 0, location.getCity(), location.getDistrict(), location.getState(), null);
    }
}
