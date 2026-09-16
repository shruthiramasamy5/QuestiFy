package com.questify.institution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.questify.institution.exception.AccessDeniedForTenantException;
import com.questify.institution.security.AuthenticatedUser;
import com.questify.institution.security.CurrentUserProvider;

class CurrentUserProviderTest {

    private final CurrentUserProvider provider = new CurrentUserProvider();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(Long institutionId, String role) {
        AuthenticatedUser user = new AuthenticatedUser("1", "a@b.test", institutionId, Set.of(role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "token", java.util.List.of()));
    }

    @Test
    void institutionAdminIsPinnedToOwnInstitution() {
        authenticate(5L, "INSTITUTION_ADMIN");

        assertThat(provider.resolveInstitutionId(null)).isEqualTo(5L);
        assertThat(provider.resolveInstitutionId(5L)).isEqualTo(5L);
        assertThatThrownBy(() -> provider.resolveInstitutionId(9L))
                .isInstanceOf(AccessDeniedForTenantException.class);
    }

    @Test
    void superAdminCanTargetAnyInstitution() {
        authenticate(null, "SUPER_ADMIN");

        assertThat(provider.resolveInstitutionId(9L)).isEqualTo(9L);
        provider.assertCanAccess(123L);
    }

    @Test
    void anonymousRequestIsRejected() {
        assertThatThrownBy(provider::require).isInstanceOf(AccessDeniedForTenantException.class);
    }
}
