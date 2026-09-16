package com.questify.institution.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.questify.institution.domain.Institution;
import com.questify.institution.domain.InstitutionStatus;
import com.questify.institution.domain.PlanTier;

public record InstitutionResponse(
        Long id,
        String name,
        String code,
        String contactEmail,
        String contactPhone,
        String address,
        String city,
        String country,
        String timezone,
        PlanTier planTier,
        InstitutionStatus status,
        Integer maxUsers,
        Integer maxPapersPerMonth,
        Integer maxStorageMb,
        BigDecimal monthlyPrice,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        Instant createdAt,
        Instant updatedAt) {

    public static InstitutionResponse from(Institution entity) {
        return new InstitutionResponse(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getAddress(),
                entity.getCity(),
                entity.getCountry(),
                entity.getTimezone(),
                entity.getPlanTier(),
                entity.getStatus(),
                entity.getMaxUsers(),
                entity.getMaxPapersPerMonth(),
                entity.getMaxStorageMb(),
                entity.getMonthlyPrice(),
                entity.getSubscriptionStartDate(),
                entity.getSubscriptionEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
