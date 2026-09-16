package com.questify.questionbank.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Mirrors the shared JWT configuration used by auth-service. */
@ConfigurationProperties(prefix = "questify.jwt")
public class JwtProperties {

    /** Base64 or plain HMAC secret shared with auth-service. */
    private String secret = "";

    /** Claim holding the granted roles. */
    private String rolesClaim = "roles";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getRolesClaim() { return rolesClaim; }
    public void setRolesClaim(String rolesClaim) { this.rolesClaim = rolesClaim; }
}
