package com.sivakaranam.ecommerce.user.controller;

import com.sivakaranam.ecommerce.common.exception.BadRequestException;
import com.sivakaranam.ecommerce.user.dto.AuthResponse;
import com.sivakaranam.ecommerce.user.dto.LoginRequest;
import com.sivakaranam.ecommerce.user.dto.RegisterRequest;
import com.sivakaranam.ecommerce.user.dto.UserResponse;
import com.sivakaranam.ecommerce.user.model.User;
import com.sivakaranam.ecommerce.user.service.AuthService;
import com.sivakaranam.ecommerce.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthService authService;
    private final UserService userService;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            UserService userService,
            @Value("${app.cookie.secure:false}") boolean cookieSecure
    ) {
        this.authService = authService;
        this.userService = userService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.TokenPair tokenPair = authService.login(request);
        setRefreshCookie(response, tokenPair.rawRefreshToken());
        return ResponseEntity.ok(tokenPair.authResponse());
    }

    /**
     * Reads the refresh token from the HttpOnly cookie, not the body. This is
     * the endpoint CSRF protection actually matters for, since the browser
     * attaches the cookie automatically on any cross-site request.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = readCookie(request, REFRESH_COOKIE_NAME)
                .orElseThrow(() -> new BadRequestException("Missing refresh token cookie"));

        AuthService.TokenPair tokenPair = authService.refresh(rawRefreshToken);
        setRefreshCookie(response, tokenPair.rawRefreshToken());
        return ResponseEntity.ok(tokenPair.authResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(JwtAuthenticationToken authentication, HttpServletResponse response) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        authService.logout(userId);
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(JwtAuthenticationToken authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(UserResponse.from(userService.findById(userId)));
    }

    private void setRefreshCookie(HttpServletResponse response, String rawRefreshToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, rawRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) authService.refreshCookieMaxAgeSeconds());
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private java.util.Optional<String> readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return java.util.Optional.empty();
        }
        return java.util.Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }
}
