package com.questify.institution.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        Long institutionId,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "code may only contain letters, digits, hyphen and underscore") String code,
        @Size(max = 150) String headName,
        @Email @Size(max = 180) String headEmail,
        @Size(max = 500) String description,
        Boolean active) {
}
