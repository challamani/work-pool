package com.workpool.user.service;

import com.workpool.common.enums.UserRole;
import com.workpool.common.exception.ResourceNotFoundException;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.common.model.GeoLocation;
import com.workpool.user.dto.AuthResponse;
import com.workpool.user.dto.LoginRequest;
import com.workpool.user.dto.RegisterRequest;
import com.workpool.user.dto.UpdateProfileRequest;
import com.workpool.user.dto.UserProfileResponse;
import com.workpool.user.model.User;
import com.workpool.user.repository.UserRepository;
import com.workpool.user.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private LoginAuditService loginAuditService;

    @InjectMocks
    private UserService userService;

    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        httpRequest = mock(HttpServletRequest.class);
        when(jwtTokenProvider.generateToken(anyString(), any(), any())).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);
    }

    @Test
    void register_happyPath_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        User saved = User.builder()
                .id("user-1")
                .email("test@example.com")
                .fullName("Test User")
                .roles(Set.of(UserRole.PUBLISHER, UserRole.FINISHER))
                .build();
        when(userRepository.save(any())).thenReturn(saved);

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
    }

    @Test
    void register_emailTaken_throwsWorkPoolException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@example.com");
        request.setPassword("password123");
        request.setFullName("User");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        WorkPoolException ex = assertThrows(WorkPoolException.class, () -> userService.register(request));
        assertEquals("EMAIL_TAKEN", ex.getErrorCode());
    }

    @Test
    void login_happyPath_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .roles(Set.of(UserRole.PUBLISHER))
                .active(true)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("test@example.com", "password"));
        when(userRepository.save(any())).thenReturn(user);

        AuthResponse response = userService.login(request, httpRequest);

        assertNotNull(response);
    }

    @Test
    void login_accountLocked_throwsWorkPoolException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("locked@example.com");
        request.setPassword("password");

        User user = User.builder()
                .id("user-1")
                .email("locked@example.com")
                .accountLockedUntil(Instant.now().plusSeconds(3600))
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(user));

        WorkPoolException ex = assertThrows(WorkPoolException.class,
                () -> userService.login(request, httpRequest));
        assertEquals("ACCOUNT_LOCKED", ex.getErrorCode());
    }

    @Test
    void login_authFails_incrementsFailedAttempts() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpwd");

        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .failedLoginAttempts(1)
                .roles(Set.of(UserRole.PUBLISHER))
                .active(true)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(userRepository.save(any())).thenReturn(user);

        assertThrows(WorkPoolException.class, () -> userService.login(request, httpRequest));
        verify(userRepository).save(any());
    }

    @Test
    void login_authFails_userNotFound_recordsAudit() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@example.com");
        request.setPassword("wrongpwd");

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(WorkPoolException.class, () -> userService.login(request, httpRequest));
        verify(loginAuditService).record(any(), anyString(), anyString(), any(boolean.class),
                anyString(), any());
    }

    @Test
    void login_authFailsAtMaxAttempts_locksAccount() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpwd");

        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .failedLoginAttempts(4)
                .roles(Set.of(UserRole.PUBLISHER))
                .active(true)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(userRepository.save(any())).thenReturn(user);

        assertThrows(WorkPoolException.class, () -> userService.login(request, httpRequest));
        verify(userRepository).save(any());
    }

    @Test
    void loginOrRegisterOAuth2_newUser_createsUser() {
        when(userRepository.findByOauthProviderAndOauthProviderId("google", "gid-1"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        User saved = User.builder()
                .id("user-new")
                .email("new@example.com")
                .roles(Set.of(UserRole.PUBLISHER, UserRole.FINISHER))
                .build();
        when(userRepository.save(any())).thenReturn(saved);

        AuthResponse response = userService.loginOrRegisterOAuth2(
                "google", "gid-1", "new@example.com", "New User", null);

        assertNotNull(response);
    }

    @Test
    void loginOrRegisterOAuth2_existingProviderUser_returnsToken() {
        User existing = User.builder()
                .id("user-1")
                .email("existing@example.com")
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findByOauthProviderAndOauthProviderId("google", "gid-1"))
                .thenReturn(Optional.of(existing));

        AuthResponse response = userService.loginOrRegisterOAuth2(
                "google", "gid-1", "existing@example.com", "User", null);

        assertNotNull(response);
    }

    @Test
    void loginOrRegisterOAuth2_existingEmailLinked_linksProvider() {
        when(userRepository.findByOauthProviderAndOauthProviderId("google", "gid-2"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByEmail("linked@example.com")).thenReturn(true);
        User existingUser = User.builder()
                .id("user-linked")
                .email("linked@example.com")
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findByEmail("linked@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);

        AuthResponse response = userService.loginOrRegisterOAuth2(
                "google", "gid-2", "linked@example.com", "Linked User", null);

        assertNotNull(response);
    }

    @Test
    void getProfile_happyPath_returnsProfile() {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .fullName("Test User")
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getProfile("user-1");

        assertNotNull(profile);
        assertEquals("test@example.com", profile.getEmail());
    }

    @Test
    void getProfile_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile("bad-id"));
    }

    @Test
    void getPublicProfile_withLocation_sanitizesCoordinates() {
        GeoLocation location = new GeoLocation(12.9, 77.6, "Bengaluru", "Bengaluru Urban",
                "Karnataka", "560001");
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .fullName("Test User")
                .location(location)
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getPublicProfile("user-1");

        assertNull(profile.getEmail());
        assertNull(profile.getPhoneNumber());
        assertNotNull(profile.getLocation());
        assertEquals(0.0, profile.getLocation().getLatitude());
        assertEquals(0.0, profile.getLocation().getLongitude());
    }

    @Test
    void getPublicProfile_nullLocation_locationRemainsNull() {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getPublicProfile("user-1");

        assertNull(profile.getLocation());
    }

    @Test
    void updateProfile_allFieldsUpdated_returnsUpdatedProfile() {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .phoneNumber("9876543210")
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber("9000000001")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setPhoneNumber("9000000001");
        request.setProfileImageUrl("https://example.com/pic.jpg");
        request.setSkills(Set.of("Java", "Python"));
        request.setLocation(new GeoLocation(0, 0, "City", "District", "State", "123456"));
        request.setServiceRadiusKm(50);
        request.setBio("New bio");

        UserProfileResponse response = userService.updateProfile("user-1", request);

        assertNotNull(response);
    }

    @Test
    void updateProfile_phoneTaken_throwsWorkPoolException() {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .phoneNumber("9876543210")
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber("9000000001")).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhoneNumber("9000000001");

        WorkPoolException ex = assertThrows(WorkPoolException.class,
                () -> userService.updateProfile("user-1", request));
        assertEquals("PHONE_TAKEN", ex.getErrorCode());
    }

    @Test
    void updateProfile_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile("bad-id", new UpdateProfileRequest()));
    }

    @Test
    void updateProfile_samePhoneNumber_noConflictCheck() {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .phoneNumber("9876543210")
                .roles(Set.of(UserRole.PUBLISHER))
                .build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhoneNumber("9876543210");

        UserProfileResponse response = userService.updateProfile("user-1", request);
        assertNotNull(response);
    }

    @Test
    void login_userNullAfterAuth_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any())).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@example.com");
        request.setPassword("password123");

        assertThrows(ResourceNotFoundException.class,
                () -> userService.login(request, mock(HttpServletRequest.class)));
    }

    @Test
    void getPublicProfile_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getPublicProfile("bad-id"));
    }
}
