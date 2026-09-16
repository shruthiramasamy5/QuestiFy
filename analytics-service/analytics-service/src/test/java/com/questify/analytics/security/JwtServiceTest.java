package com.questify.analytics.security;

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

    @Test
    void parsesFacultyAndHodRoles() {
        String token = Jwts.builder()
                .subject("user-1")
                .claims(Map.of("roles", List.of("FACULTY", "HOD"), "institutionId", "inst-1"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Claims claims = jwtService.parse(token);
        assertThat(jwtService.authorities(claims)).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_FACULTY", "ROLE_HOD");
    }

    @Test
    void rejectsUnsignedToken() {
        assertThatThrownBy(() -> jwtService.parse("a.b.c")).isInstanceOf(JwtException.class);
    }
}
