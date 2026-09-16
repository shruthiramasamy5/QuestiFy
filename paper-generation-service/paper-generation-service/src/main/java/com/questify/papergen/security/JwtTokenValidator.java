package com.questify.papergen.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Validates the QuestiFy JWT issued by auth/institution service.
 * The same HS256 secret (questify.jwt.secret) is shared by all services.
 */
@Component
public class JwtTokenValidator {

    private final SecretKey key;
    private final String rolesClaim;

    public JwtTokenValidator(@Value("${questify.jwt.secret}") String secret,
                             @Value("${questify.jwt.roles-claim:roles}") String rolesClaim) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.rolesClaim = rolesClaim;
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        Object raw = claims.get(rolesClaim);
        if (raw == null) {
            raw = claims.get("role");
        }
        List<String> roles = new ArrayList<>();
        if (raw instanceof Collection<?> collection) {
            for (Object o : collection) {
                if (o != null) roles.add(String.valueOf(o));
            }
        } else if (raw instanceof String s && !s.isBlank()) {
            for (String part : s.split("[,\\s]+")) {
                if (!part.isBlank()) {
                    roles.add(part);
                }
            }
        }
        return roles.stream()
                .map(String::trim)
                .filter(r -> !r.isEmpty())
                .map(r -> {
                    String norm = r.toUpperCase(java.util.Locale.ROOT).replace('-', '_').replace(' ', '_');
                    return norm.startsWith("ROLE_") ? norm.substring(5) : norm;
                })
                .distinct()
                .toList();
    }
}
