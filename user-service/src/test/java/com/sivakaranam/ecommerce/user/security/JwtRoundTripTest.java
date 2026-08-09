package com.sivakaranam.ecommerce.user.security;

import com.sivakaranam.ecommerce.user.model.Role;
import com.sivakaranam.ecommerce.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wires up the real JwtService and the real JwtDecoder bean together, rather
 * than mocking either side, so a signing/verification mismatch between them
 * fails here instead of only at runtime.
 */
@SpringBootTest
@ActiveProfiles("test")
class JwtRoundTripTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void tokenIssuedByJwtService_isAcceptedByTheSharedResourceServerDecoder() {
        User user = new User();
        user.setId(42L);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        Role role = new Role();
        role.setName("CUSTOMER");
        user.setRoles(Set.of(role));

        String token = jwtService.issueAccessToken(user);

        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("42");
        assertThat(decoded.getClaimAsString("email")).isEqualTo("jane@example.com");
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly("CUSTOMER");
    }
}
