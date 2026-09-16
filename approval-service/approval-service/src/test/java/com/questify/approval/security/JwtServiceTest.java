package com.questify.approval.security;

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
                .claims(Map.of("roles", roles, "email", "hod@questify.test", "institutionId", "inst-1"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void parsesRolesFromListClaim() {
        Claims claims = jwtService.parse(token(List.of("HOD", "faculty")));
        assertThat(jwtService.authorities(claims))
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_HOD", "ROLE_FACULTY");
    }

    @Test
    void parsesRolesFromStringClaim() {
        Claims claims = jwtService.parse(token("REVIEWER,HOD"));
        assertThat(jwtService.authorities(claims)).hasSize(2);
    }

    @Test
    void rejectsTamperedToken() {
        String tampered = token(List.of("HOD")) + "x";
        assertThatThrownBy(() -> jwtService.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void buildsAuthenticatedUser() {
        Claims claims = jwtService.parse(token(List.of("HOD")));
        AuthenticatedUser user = AuthenticatedUser.of(claims.getSubject(), claims.get("email", String.class),
                claims.get("institutionId", String.class), jwtService.authorities(claims));
        assertThat(user.hasRole("HOD")).isTrue();
        assertThat(user.hasRole("REVIEWER")).isFalse();
        assertThat(user.institutionId()).isEqualTo("inst-1");
    }
}
