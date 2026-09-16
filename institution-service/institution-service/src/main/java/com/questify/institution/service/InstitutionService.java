package com.questify.institution.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.questify.institution.domain.Institution;
import com.questify.institution.domain.InstitutionStatus;
import com.questify.institution.domain.PlanTier;
import com.questify.institution.dto.InstitutionRequest;
import com.questify.institution.dto.InstitutionResponse;
import com.questify.institution.exception.DuplicateResourceException;
import com.questify.institution.exception.ResourceNotFoundException;
import com.questify.institution.repository.DepartmentRepository;
import com.questify.institution.repository.InstitutionRepository;
import com.questify.institution.repository.PlanUsageRepository;
import com.questify.institution.repository.TemplateRepository;
import com.questify.institution.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final TemplateRepository templateRepository;
    private final PlanUsageRepository planUsageRepository;
    private final CurrentUserProvider currentUserProvider;

    public InstitutionService(InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            TemplateRepository templateRepository,
            PlanUsageRepository planUsageRepository,
            CurrentUserProvider currentUserProvider) {
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.templateRepository = templateRepository;
        this.planUsageRepository = planUsageRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public Page<InstitutionResponse> list(String search, InstitutionStatus status, Pageable pageable) {
        Page<Institution> page;
        if (search != null && !search.isBlank()) {
            page = institutionRepository
                    .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(search, search, pageable);
        } else if (status != null) {
            page = institutionRepository.findByStatus(status, pageable);
        } else {
            page = institutionRepository.findAll(pageable);
        }
        return page.map(InstitutionResponse::from);
    }

    public InstitutionResponse get(Long id) {
        currentUserProvider.assertCanAccess(id);
        return InstitutionResponse.from(getEntity(id));
    }

    public InstitutionResponse getCurrent() {
        Long institutionId = currentUserProvider.resolveInstitutionId(null);
        return InstitutionResponse.from(getEntity(institutionId));
    }

    public Institution getEntity(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", id));
    }

    @Transactional
    public InstitutionResponse create(InstitutionRequest request) {
        if (institutionRepository.existsByCodeIgnoreCase(request.code())) {
            throw new DuplicateResourceException("An institution with code '" + request.code() + "' already exists");
        }
        Institution entity = new Institution();
        apply(entity, request);
        return InstitutionResponse.from(institutionRepository.save(entity));
    }

    @Transactional
    public InstitutionResponse update(Long id, InstitutionRequest request) {
        currentUserProvider.assertCanAccess(id);
        Institution entity = getEntity(id);
        if (!entity.getCode().equalsIgnoreCase(request.code())
                && institutionRepository.existsByCodeIgnoreCase(request.code())) {
            throw new DuplicateResourceException("An institution with code '" + request.code() + "' already exists");
        }
        apply(entity, request);
        return InstitutionResponse.from(institutionRepository.save(entity));
    }

    @Transactional
    public InstitutionResponse updateStatus(Long id, InstitutionStatus status) {
        Institution entity = getEntity(id);
        entity.setStatus(status);
        return InstitutionResponse.from(institutionRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Institution entity = getEntity(id);
        departmentRepository.deleteByInstitutionId(entity.getId());
        templateRepository.deleteByInstitutionId(entity.getId());
        planUsageRepository.deleteByInstitutionId(entity.getId());
        institutionRepository.delete(entity);
    }

    private void apply(Institution entity, InstitutionRequest request) {
        entity.setName(request.name().trim());
        entity.setCode(request.code().trim().toUpperCase());
        entity.setContactEmail(request.contactEmail().trim());
        entity.setContactPhone(request.contactPhone());
        entity.setAddress(request.address());
        entity.setCity(request.city());
        entity.setCountry(request.country());
        entity.setTimezone(request.timezone());
        if (request.planTier() != null) {
            entity.setPlanTier(request.planTier());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.maxUsers() != null) {
            entity.setMaxUsers(request.maxUsers());
        }
        if (request.maxPapersPerMonth() != null) {
            entity.setMaxPapersPerMonth(request.maxPapersPerMonth());
        }
        if (request.maxStorageMb() != null) {
            entity.setMaxStorageMb(request.maxStorageMb());
        }
        entity.setMonthlyPrice(request.monthlyPrice());
        entity.setSubscriptionStartDate(request.subscriptionStartDate());
        entity.setSubscriptionEndDate(request.subscriptionEndDate());
        if (entity.getPlanTier() == null) {
            entity.setPlanTier(PlanTier.FREE);
        }
    }
}
