package com.questify.institution;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.questify.institution.config.JwtProperties;
import com.questify.institution.security.AuthenticatedUser;
import com.questify.institution.security.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-institution-service-unit-tests-0123456789";

    @Test
    void parsesSubjectRolesAndInstitutionClaim() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        JwtService jwtService = new JwtService(properties);

        String token = Jwts.builder()
                .subject("42")
                .claim("email", "admin@questify.test")
                .claim("institutionId", 7)
                .claim("roles", List.of("ROLE_INSTITUTION_ADMIN"))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        AuthenticatedUser user = jwtService.parse(token);

        assertThat(user.userId()).isEqualTo("42");
        assertThat(user.email()).isEqualTo("admin@questify.test");
        assertThat(user.institutionId()).isEqualTo(7L);
        assertThat(user.roles()).containsExactly("INSTITUTION_ADMIN");
        assertThat(user.isSuperAdmin()).isFalse();
    }
}
