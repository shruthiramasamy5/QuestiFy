package com.questify.auth.dto;

import com.questify.auth.entity.Role;

public record UpdateUserRequest(
        String name,
        String email,
        String password,
        Role role,
        String institutionName,
        Long institutionId,
        Boolean active,
        String department
) {
}
