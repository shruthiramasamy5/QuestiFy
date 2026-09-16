package com.questify.institution.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.questify.institution.exception.AccessDeniedForTenantException;

@Component
public class CurrentUserProvider {

    public Optional<AuthenticatedUser> find() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public AuthenticatedUser require() {
        return find().orElseThrow(() -> new AccessDeniedForTenantException("No authenticated user in context"));
    }

    /**
     * Resolves the institution a request should operate on. Super admins may target any
     * institution; every other caller is pinned to the institution in their token.
     */
    public Long resolveInstitutionId(Long requested) {
        AuthenticatedUser user = require();
        if (user.isSuperAdmin()) {
            if (requested != null) {
                return requested;
            }
            if (user.institutionId() != null) {
                return user.institutionId();
            }
            throw new AccessDeniedForTenantException("institutionId query parameter is required for super admins");
        }
        Long own = user.institutionId();
        if (own == null) {
            throw new AccessDeniedForTenantException("Token does not carry an institution claim");
        }
        if (requested != null && !requested.equals(own)) {
            throw new AccessDeniedForTenantException("Access to another institution is not allowed");
        }
        return own;
    }

    public void assertCanAccess(Long institutionId) {
        AuthenticatedUser user = require();
        if (user.isSuperAdmin()) {
            return;
        }
        if (institutionId == null || !institutionId.equals(user.institutionId())) {
            throw new AccessDeniedForTenantException("Access to another institution is not allowed");
        }
    }
}
