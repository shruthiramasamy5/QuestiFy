package com.questify.analytics.security;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

/** Principal placed in the {@code SecurityContext} after a JWT is validated. */
public record AuthenticatedUser(String userId, String email, String institutionId, Set<String> roles) {

    public static AuthenticatedUser of(String userId, String email, String institutionId,
                                       Collection<? extends GrantedAuthority> authorities) {
        Set<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(value -> value.startsWith("ROLE_") ? value.substring(5) : value)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return new AuthenticatedUser(userId, email, institutionId, roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role.toUpperCase(Locale.ROOT));
    }
}
