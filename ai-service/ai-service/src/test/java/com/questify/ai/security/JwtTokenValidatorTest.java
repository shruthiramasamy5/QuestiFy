package com.questify.ai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenValidatorTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-please-change-32";

    private final JwtTokenValidator validator = new JwtTokenValidator(SECRET, "roles");

    private String issueToken(String subject, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void parsesValidTokenAndExtractsRoles() {
        String token = issueToken("user-1", List.of("FACULTY", "COURSE_COORDINATOR"));

        Claims claims = validator.parse(token);

        assertThat(claims.getSubject()).isEqualTo("user-1");
        assertThat(validator.extractRoles(claims)).containsExactly("FACULTY", "COURSE_COORDINATOR");
    }

    @Test
    void rejectsTokenSignedWithWrongKey() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("a-completely-different-32-byte-secret!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("user-2").signWith(wrongKey).compact();

        assertThatThrownBy(() -> validator.parse(token)).isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }

    @Test
    void extractRolesHandlesCommaSeparatedStringClaim() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user-3")
                .claims(Map.of("roles", "HOD, FACULTY"))
                .signWith(key)
                .compact();

        Claims claims = validator.parse(token);

        assertThat(validator.extractRoles(claims)).containsExactly("HOD", "FACULTY");
    }
}
