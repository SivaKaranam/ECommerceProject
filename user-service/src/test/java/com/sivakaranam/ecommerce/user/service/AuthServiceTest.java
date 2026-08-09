package com.sivakaranam.ecommerce.user.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.user.dto.LoginRequest;
import com.sivakaranam.ecommerce.user.model.Role;
import com.sivakaranam.ecommerce.user.model.User;
import com.sivakaranam.ecommerce.user.security.JwtService;
import com.sivakaranam.ecommerce.user.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, passwordEncoder, jwtService, refreshTokenService);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Test User");
        existingUser.setEmail("test@example.com");
        existingUser.setPasswordHash("hashed-password");
        Role customerRole = new Role();
        customerRole.setName("CUSTOMER");
        existingUser.setRoles(Set.of(customerRole));
    }

    @Test
    void login_withCorrectPassword_issuesAccessAndRefreshTokens() {
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtService.issueAccessToken(existingUser)).thenReturn("access-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(existingUser)).thenReturn("raw-refresh-token");

        AuthService.TokenPair tokenPair = authService.login(new LoginRequest("test@example.com", "correct-password"));

        assertThat(tokenPair.authResponse().accessToken()).isEqualTo("access-token");
        assertThat(tokenPair.authResponse().expiresInSeconds()).isEqualTo(900L);
        assertThat(tokenPair.rawRefreshToken()).isEqualTo("raw-refresh-token");
    }

    @Test
    void login_withWrongPassword_throwsBadRequest() {
        when(userService.findByEmail("test@example.com")).thenReturn(existingUser);
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrong-password")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void refresh_withBlankToken_throwsBadRequest_withoutTouchingTheDatabase() {
        assertThatThrownBy(() -> authService.refresh(" "))
                .isInstanceOf(BadRequestException.class);

        org.mockito.Mockito.verifyNoInteractions(refreshTokenService);
    }
}
