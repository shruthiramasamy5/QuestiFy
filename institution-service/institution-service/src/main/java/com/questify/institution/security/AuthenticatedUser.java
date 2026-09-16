package com.questify.institution.security;

import java.util.Set;

/** Principal built from the auth-service issued JWT. */
public record AuthenticatedUser(String userId, String email, Long institutionId, Set<String> roles) {

    public boolean isSuperAdmin() {
        return roles.contains("SUPER_ADMIN");
    }
}
