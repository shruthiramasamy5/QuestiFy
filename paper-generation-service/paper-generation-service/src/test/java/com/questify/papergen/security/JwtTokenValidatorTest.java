package com.questify.papergen.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenValidatorTest {

    private static final String SECRET = "questify-shared-development-secret-key-change-me-32bytes";

    private String token(List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("faculty1")
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void parsesSubjectAndRoles() {
        JwtTokenValidator validator = new JwtTokenValidator(SECRET, "roles");
        Claims claims = validator.parse(token(List.of("FACULTY", "COURSE_COORDINATOR")));
        assertEquals("faculty1", claims.getSubject());
        assertTrue(validator.extractRoles(claims).contains("FACULTY"));
        assertEquals(2, validator.extractRoles(claims).size());
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        JwtTokenValidator other = new JwtTokenValidator("a-completely-different-secret-key-value-32bytes!!", "roles");
        String t = token(List.of("FACULTY"));
        assertThrows(Exception.class, () -> other.parse(t));
    }
}
