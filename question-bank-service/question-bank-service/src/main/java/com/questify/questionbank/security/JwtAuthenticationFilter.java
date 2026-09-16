package com.questify.questionbank.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator validator;
    private final JwtProperties properties;

    public JwtAuthenticationFilter(JwtTokenValidator validator, JwtProperties properties) {
        this.validator = validator;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Claims claims = validator.parse(header.substring(7));
            if (claims != null) {
                Collection<GrantedAuthority> authorities = extractAuthorities(claims);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Object raw = claims.get(properties.getRolesClaim());
        if (raw == null) {
            raw = claims.get("role");
        }
        List<String> roles;
        if (raw instanceof Collection<?> collection) {
            roles = collection.stream().filter(Objects::nonNull).map(Object::toString).toList();
        } else if (raw instanceof String value && !value.isBlank()) {
            roles = List.of(value.split("[,\\s]+"));
        } else {
            roles = List.of();
        }
        return roles.stream()
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(role -> {
                    String norm = role.toUpperCase(java.util.Locale.ROOT).replace('-', '_').replace(' ', '_');
                    return norm.startsWith("ROLE_") ? norm : "ROLE_" + norm;
                })
                .distinct()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();
    }
}
