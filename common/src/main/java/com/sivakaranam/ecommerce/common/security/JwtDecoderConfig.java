package com.sivakaranam.ecommerce.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Every service validates access tokens with the same shared secret user-service
 * signs them with, instead of fetching JWKS over the network at startup. That
 * keeps services independently bootable (no hard dependency on user-service being
 * up first) which matters for tests and for local/CI startup ordering.
 */
@Configuration
public class JwtDecoderConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
