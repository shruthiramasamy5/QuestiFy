package com.questify.auth.dto;

import com.questify.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        String password,
        @NotNull(message = "Role is required") Role role,
        String institutionName,
        Long institutionId,
        String department
) {
}
