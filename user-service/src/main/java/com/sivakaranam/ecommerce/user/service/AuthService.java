package com.sivakaranam.ecommerce.user.service;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.user.dto.AuthResponse;
import com.sivakaranam.ecommerce.user.dto.LoginRequest;
import com.sivakaranam.ecommerce.user.model.User;
import com.sivakaranam.ecommerce.user.security.JwtService;
import com.sivakaranam.ecommerce.user.security.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public TokenPair login(LoginRequest request) {
        User user = userService.findByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        return issueTokenPair(user);
    }

    public TokenPair refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BadRequestException("Missing refresh token");
        }
        User user = refreshTokenService.validateAndRevoke(rawRefreshToken);
        return issueTokenPair(user);
    }

    public void logout(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private TokenPair issueTokenPair(User user) {
        String accessToken = jwtService.issueAccessToken(user);
        String rawRefreshToken = refreshTokenService.issue(user);
        AuthResponse authResponse = new AuthResponse(accessToken, "Bearer", jwtService.accessTokenTtlSeconds());
        return new TokenPair(authResponse, rawRefreshToken);
    }

    public long refreshCookieMaxAgeSeconds() {
        return refreshTokenService.ttlSeconds();
    }

    public record TokenPair(AuthResponse authResponse, String rawRefreshToken) {
    }
}
