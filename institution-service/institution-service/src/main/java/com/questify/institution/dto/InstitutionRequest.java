package com.questify.institution.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.questify.institution.domain.InstitutionStatus;
import com.questify.institution.domain.PlanTier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record InstitutionRequest(
        @NotBlank @Size(max = 180) String name,
        @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "code may only contain letters, digits, hyphen and underscore") String code,
        @NotBlank @Email @Size(max = 180) String contactEmail,
        @Size(max = 40) String contactPhone,
        @Size(max = 400) String address,
        @Size(max = 120) String city,
        @Size(max = 120) String country,
        @Size(max = 64) String timezone,
        PlanTier planTier,
        InstitutionStatus status,
        @Min(1) Integer maxUsers,
        @Min(1) Integer maxPapersPerMonth,
        @Min(1) Integer maxStorageMb,
        @PositiveOrZero BigDecimal monthlyPrice,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate) {
}
