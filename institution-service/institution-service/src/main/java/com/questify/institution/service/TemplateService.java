package com.questify.institution.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.questify.institution.domain.Template;
import com.questify.institution.domain.TemplateType;
import com.questify.institution.dto.TemplateRequest;
import com.questify.institution.dto.TemplateResponse;
import com.questify.institution.exception.DuplicateResourceException;
import com.questify.institution.exception.ResourceNotFoundException;
import com.questify.institution.repository.DepartmentRepository;
import com.questify.institution.repository.InstitutionRepository;
import com.questify.institution.repository.TemplateRepository;
import com.questify.institution.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserProvider currentUserProvider;

    public TemplateService(TemplateRepository templateRepository,
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            CurrentUserProvider currentUserProvider) {
        this.templateRepository = templateRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public Page<TemplateResponse> list(Long institutionId, TemplateType type, Pageable pageable) {
        Long scoped = currentUserProvider.resolveInstitutionId(institutionId);
        Page<Template> page = type == null
                ? templateRepository.findByInstitutionId(scoped, pageable)
                : templateRepository.findByInstitutionIdAndType(scoped, type, pageable);
        return page.map(TemplateResponse::from);
    }

    public TemplateResponse get(Long id) {
        return TemplateResponse.from(getEntity(id));
    }

    @Transactional
    public TemplateResponse create(TemplateRequest request) {
        Long institutionId = currentUserProvider.resolveInstitutionId(request.institutionId());
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", institutionId);
        }
        if (templateRepository.existsByInstitutionIdAndNameIgnoreCase(institutionId, request.name())) {
            throw new DuplicateResourceException("A template named '" + request.name() + "' already exists");
        }
        Template entity = new Template();
        entity.setInstitutionId(institutionId);
        apply(entity, request);
        Template saved = templateRepository.save(entity);
        if (saved.isDefaultTemplate()) {
            clearOtherDefaults(saved);
        }
        return TemplateResponse.from(saved);
    }

    @Transactional
    public TemplateResponse update(Long id, TemplateRequest request) {
        Template entity = getEntity(id);
        if (!entity.getName().equalsIgnoreCase(request.name())
                && templateRepository.existsByInstitutionIdAndNameIgnoreCase(entity.getInstitutionId(),
                        request.name())) {
            throw new DuplicateResourceException("A template named '" + request.name() + "' already exists");
        }
        apply(entity, request);
        Template saved = templateRepository.save(entity);
        if (saved.isDefaultTemplate()) {
            clearOtherDefaults(saved);
        }
        return TemplateResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        templateRepository.delete(getEntity(id));
    }

    private Template getEntity(Long id) {
        Template entity = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));
        currentUserProvider.assertCanAccess(entity.getInstitutionId());
        return entity;
    }

    private void clearOtherDefaults(Template current) {
        templateRepository.findByInstitutionIdAndDefaultTemplateTrue(current.getInstitutionId()).stream()
                .filter(other -> !other.getId().equals(current.getId()))
                .filter(other -> other.getType() == current.getType())
                .forEach(other -> {
                    other.setDefaultTemplate(false);
                    templateRepository.save(other);
                });
    }

    private void apply(Template entity, TemplateRequest request) {
        if (request.departmentId() != null) {
            departmentRepository.findByIdAndInstitutionId(request.departmentId(), entity.getInstitutionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));
        }
        entity.setDepartmentId(request.departmentId());
        entity.setName(request.name().trim());
        entity.setType(request.type());
        entity.setDescription(request.description());
        entity.setHeaderHtml(request.headerHtml());
        entity.setFooterHtml(request.footerHtml());
        entity.setInstructions(request.instructions());
        entity.setLayoutJson(request.layoutJson());
        if (request.defaultTemplate() != null) {
            entity.setDefaultTemplate(request.defaultTemplate());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }
}
