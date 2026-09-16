package com.questify.institution.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.questify.institution.domain.PlanUsage;

public interface PlanUsageRepository extends JpaRepository<PlanUsage, Long> {

    Optional<PlanUsage> findByInstitutionIdAndPeriod(Long institutionId, String period);

    List<PlanUsage> findByInstitutionIdOrderByPeriodDesc(Long institutionId);

    void deleteByInstitutionId(Long institutionId);
}
