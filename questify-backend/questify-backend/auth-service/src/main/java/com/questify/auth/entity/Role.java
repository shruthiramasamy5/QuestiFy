package com.questify.auth.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Mirrors ROLES in questify-frontend/src/utils/roles.js exactly.
 * The wire value (JSON + JWT claim) is the hyphenated string, not the enum name —
 * that string is also what the frontend uses to key navigation.js and route guards.
 */
public enum Role {
    SUPER_ADMIN("super-admin"),
    INSTITUTION_ADMIN("institution-admin"),
    FACULTY("faculty"),
    HOD("hod"),
    REVIEWER("reviewer"),
    COURSE_COORDINATOR("course-coordinator");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        String clean = value.trim().toUpperCase().replace("-", "_");
        if (clean.startsWith("ROLE_")) clean = clean.substring(5);
        for (Role role : values()) {
            if (role.name().equals(clean) || role.value.equalsIgnoreCase(value.trim())) return role;
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
