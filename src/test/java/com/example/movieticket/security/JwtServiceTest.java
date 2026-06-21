package com.example.movieticket.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.movieticket.domain.User;
import com.example.movieticket.support.factory.UserFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 60));

    private Claims parse(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    @Test
    void tokenCarriesEmailRoleAndUserId() {
        User user = UserFactory.withId(42L, UserFactory.customer("alice@example.com"));

        Claims claims = parse(jwtService.generateToken(user));

        assertThat(claims.getSubject()).isEqualTo("alice@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.get("uid", Long.class)).isEqualTo(42L);
    }

    @Test
    void tokenExpiresInTheFuture() {
        User user = UserFactory.withId(1L, UserFactory.admin("bob@example.com"));

        Claims claims = parse(jwtService.generateToken(user));

        assertThat(claims.getExpiration()).isAfter(new Date());
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
