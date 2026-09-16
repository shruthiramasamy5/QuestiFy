package com.questify.questionbank.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

/**
 * Shared JWT validation pattern: tokens are issued by auth-service and signed
 * with the same HMAC secret, so every microservice validates them locally.
 */
@Component
public class JwtTokenValidator {

    private final SecretKey key;

    public JwtTokenValidator(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(decode(properties.getSecret()));
    }

    private static byte[] decode(String secret) {
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    /** @return parsed claims, or null when the token is missing/invalid/expired. */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
