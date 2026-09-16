package com.questify.institution.service;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.questify.institution.domain.Institution;
import com.questify.institution.domain.PlanUsage;
import com.questify.institution.dto.BillingSummaryResponse;
import com.questify.institution.dto.PlanUsageResponse;
import com.questify.institution.dto.PlanUsageStatsResponse;
import com.questify.institution.dto.PlanUsageStatsResponse.UsageMetric;
import com.questify.institution.dto.UsageRecordRequest;
import com.questify.institution.exception.ResourceNotFoundException;
import com.questify.institution.repository.DepartmentRepository;
import com.questify.institution.repository.InstitutionRepository;
import com.questify.institution.repository.PlanUsageRepository;
import com.questify.institution.repository.TemplateRepository;
import com.questify.institution.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class PlanUsageService {

    private final PlanUsageRepository planUsageRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final TemplateRepository templateRepository;
    private final InstitutionService institutionService;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    public PlanUsageService(PlanUsageRepository planUsageRepository,
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            TemplateRepository templateRepository,
            InstitutionService institutionService,
            CurrentUserProvider currentUserProvider,
            Clock clock) {
        this.planUsageRepository = planUsageRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.templateRepository = templateRepository;
        this.institutionService = institutionService;
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
    }

    public String currentPeriod() {
        return YearMonth.now(clock).toString();
    }

    public PlanUsageStatsResponse stats(Long institutionId) {
        Long scoped = currentUserProvider.resolveInstitutionId(institutionId);
        Institution institution = institutionService.getEntity(scoped);
        String period = currentPeriod();
        PlanUsage usage = planUsageRepository.findByInstitutionIdAndPeriod(scoped, period)
                .orElseGet(() -> emptyUsage(scoped, period));

        List<PlanUsageResponse> history = planUsageRepository.findByInstitutionIdOrderByPeriodDesc(scoped).stream()
                .map(PlanUsageResponse::from)
                .toList();

        return new PlanUsageStatsResponse(
                institution.getId(),
                institution.getName(),
                institution.getPlanTier(),
                institution.getStatus(),
                institution.getMonthlyPrice(),
                institution.getSubscriptionStartDate(),
                institution.getSubscriptionEndDate(),
                period,
                UsageMetric.of(usage.getPapersGenerated(), institution.getMaxPapersPerMonth()),
                UsageMetric.of(usage.getActiveUsers(), institution.getMaxUsers()),
                UsageMetric.of(usage.getStorageUsedMb(), institution.getMaxStorageMb()),
                usage.getQuestionsCreated(),
                (int) departmentRepository.countByInstitutionId(scoped),
                (int) templateRepository.countByInstitutionId(scoped),
                history);
    }

    public List<PlanUsageResponse> history(Long institutionId) {
        Long scoped = currentUserProvider.resolveInstitutionId(institutionId);
        return planUsageRepository.findByInstitutionIdOrderByPeriodDesc(scoped).stream()
                .map(PlanUsageResponse::from)
                .toList();
    }

    public List<BillingSummaryResponse> billingSummary() {
        String period = currentPeriod();
        return institutionRepository.findAll().stream()
                .map(institution -> {
                    Optional<PlanUsage> usage = planUsageRepository
                            .findByInstitutionIdAndPeriod(institution.getId(), period);
                    return new BillingSummaryResponse(
                            institution.getId(),
                            institution.getName(),
                            institution.getCode(),
                            institution.getPlanTier(),
                            institution.getStatus(),
                            institution.getMonthlyPrice(),
                            institution.getSubscriptionStartDate(),
                            institution.getSubscriptionEndDate(),
                            period,
                            usage.map(PlanUsage::getPapersGenerated).orElse(0),
                            usage.map(PlanUsage::getActiveUsers).orElse(0),
                            usage.map(PlanUsage::getStorageUsedMb).orElse(0));
                })
                .toList();
    }

    @Transactional
    public PlanUsageResponse record(UsageRecordRequest request) {
        Long institutionId = request.institutionId();
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", institutionId);
        }
        String period = request.period() == null || request.period().isBlank() ? currentPeriod() : request.period();
        PlanUsage usage = planUsageRepository.findByInstitutionIdAndPeriod(institutionId, period)
                .orElseGet(() -> emptyUsage(institutionId, period));

        if (request.papersGenerated() != null) {
            usage.setPapersGenerated(usage.getPapersGenerated() + request.papersGenerated());
        }
        if (request.questionsCreated() != null) {
            usage.setQuestionsCreated(usage.getQuestionsCreated() + request.questionsCreated());
        }
        if (request.activeUsers() != null) {
            usage.setActiveUsers(request.activeUsers());
        }
        if (request.storageUsedMb() != null) {
            usage.setStorageUsedMb(request.storageUsedMb());
        }
        return PlanUsageResponse.from(planUsageRepository.save(usage));
    }

    private PlanUsage emptyUsage(Long institutionId, String period) {
        PlanUsage usage = new PlanUsage();
        usage.setInstitutionId(institutionId);
        usage.setPeriod(period);
        return usage;
    }
}
