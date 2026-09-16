package com.questify.institution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

/**
 * Mirrors the auth-service JWT configuration. The same {@code questify.jwt.secret}
 * used to sign tokens in auth-service must be provided to this service so issued
 * tokens can be validated locally without a network call.
 */
@ConfigurationProperties(prefix = "questify.jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    private String issuer = "questify-auth-service";

    private String rolesClaim = "roles";

    private String institutionClaim = "institutionId";

    private String emailClaim = "email";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
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

    public String getEmailClaim() {
        return emailClaim;
    }

    public void setEmailClaim(String emailClaim) {
        this.emailClaim = emailClaim;
    }
}
