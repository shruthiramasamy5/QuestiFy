package com.questify.auth.dto;

import com.questify.auth.entity.Role;
import com.questify.auth.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String institutionName,
        Long institutionId
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getInstitutionName(),
                user.getInstitutionId()
        );
    }
}
