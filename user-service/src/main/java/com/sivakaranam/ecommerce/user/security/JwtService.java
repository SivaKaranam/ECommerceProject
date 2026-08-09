package com.sivakaranam.ecommerce.user.security;

import com.sivakaranam.ecommerce.user.model.Role;
import com.sivakaranam.ecommerce.user.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Issues the access tokens every other service validates. Signed with the same
 * HMAC secret product-service/order-service/payment-service use to decode
 * (see common's JwtDecoderConfig), so there's a single source of truth for who's
 * allowed in, without services having to call each other over the network to
 * check a token.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenTtlMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", roleNames)
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(now.plus(accessTokenTtlMinutes, ChronoUnit.MINUTES)))
                // Explicit algorithm so it always matches common's resource-server decoder.
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return accessTokenTtlMinutes * 60;
    }
}
