package com.questify.approval.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Parses and validates the JWT issued by the QuestiFy auth-service.
 * The same HMAC secret is shared by all services, so no remote call is needed.
 */
@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** @throws JwtException when the token is expired, malformed or badly signed. */
    public Claims parse(String token) {
        Jws<Claims> jws = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
        return jws.getPayload();
    }

    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> authorities(Claims claims) {
        Object raw = claims.get(properties.getRolesClaim());
        if (raw == null) {
            raw = claims.get("role");
        }
        List<String> roles = new ArrayList<>();
        if (raw instanceof Collection<?> collection) {
            collection.forEach(value -> {
                if (value != null) roles.add(String.valueOf(value));
            });
        } else if (raw instanceof String string && !string.isBlank()) {
            for (String part : string.split("[,\\s]+")) {
                if (!part.isBlank()) {
                    roles.add(part);
                }
            }
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            String normalized = role.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
            authorities.add(new SimpleGrantedAuthority(
                    normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized));
        }
        return authorities;
    }
}
