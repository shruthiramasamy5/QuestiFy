package com.questify.audit.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code questify.jwt.*} properties shared by every QuestiFy service. */
@ConfigurationProperties(prefix = "questify.jwt")
public class JwtProperties {

    /** HMAC-SHA secret shared with auth-service. Must be at least 32 bytes. */
    private String secret = "change-me-change-me-change-me-change-me";

    /** Name of the claim that carries the user roles. */
    private String rolesClaim = "roles";

    /** Name of the claim that carries the institution id (multi tenancy). */
    private String institutionClaim = "institutionId";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRolesClaim() {
        return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    public String getInstitutionClaim() {
        return institutionClaim;
    }

    public void setInstitutionClaim(String institutionClaim) {
        this.institutionClaim = institutionClaim;
    }
}
