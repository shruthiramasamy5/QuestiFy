package com.questify.institution.security;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.questify.institution.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Validates JWTs issued by auth-service using the shared HMAC secret
 * ({@code questify.jwt.secret}).
 */
@Component
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = buildKey(properties.getSecret());
    }

    private static SecretKey buildKey(String secret) {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthenticatedUser(
                claims.getSubject(),
                claims.get(properties.getEmailClaim(), String.class),
                readInstitutionId(claims),
                readRoles(claims));
    }

    private Long readInstitutionId(Claims claims) {
        Object raw = claims.get(properties.getInstitutionClaim());
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(raw.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Set<String> readRoles(Claims claims) {
        Set<String> roles = new LinkedHashSet<>();
        Object raw = claims.get(properties.getRolesClaim());
        if (raw == null) {
            raw = claims.get("role");
        }
        if (raw instanceof List<?> list) {
            list.forEach(item -> addRole(roles, String.valueOf(item)));
        } else if (raw != null) {
            for (String part : raw.toString().split("[,\\s]+")) {
                addRole(roles, part);
            }
        }
        return roles;
    }

    private void addRole(Set<String> roles, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        roles.add(normalized);
    }
}
