package com.workpool.user.security;

import com.workpool.common.enums.UserRole;
import com.workpool.user.model.User;
import com.workpool.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_foundUser_returnsUserDetails() {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .password("hashed-password")
                .roles(Set.of(UserRole.PUBLISHER))
                .active(true)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("test@example.com");

        assertEquals("user-1", details.getUsername());
        assertEquals("hashed-password", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_foundUserNullPassword_returnsUserDetailsWithEmptyPassword() {
        User user = User.builder()
                .id("user-2")
                .email("oauth@example.com")
                .password(null)
                .roles(Set.of(UserRole.FINISHER))
                .active(true)
                .build();
        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("oauth@example.com");

        assertEquals("", details.getPassword());
    }

    @Test
    void loadUserByUsername_inactiveUser_accountLocked() {
        User user = User.builder()
                .id("user-3")
                .email("inactive@example.com")
                .password("pwd")
                .roles(Set.of(UserRole.PUBLISHER))
                .active(false)
                .build();
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("inactive@example.com");

        assertTrue(details.isAccountNonExpired());
        assertTrue(!details.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com"));
    }
}
