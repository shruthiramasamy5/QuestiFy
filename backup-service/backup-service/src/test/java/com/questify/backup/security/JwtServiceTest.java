package com.questify.backup.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        jwtService = new JwtService(properties);
    }

    private String token(Object roles) {
        return Jwts.builder()
                .subject("user-1")
                .claims(Map.of("roles", roles, "email", "rev@questify.test", "institutionId", "inst-1"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void mapsReviewerRole() {
        Claims claims = jwtService.parse(token(List.of("Reviewer")));
        assertThat(jwtService.authorities(claims)).extracting("authority").containsExactly("ROLE_REVIEWER");
    }

    @Test
    void rejectsGarbageToken() {
        assertThatThrownBy(() -> jwtService.parse("not-a-token")).isInstanceOf(JwtException.class);
    }
}
