package com.questify.institution.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.questify.institution.domain.Department;
import com.questify.institution.dto.DepartmentRequest;
import com.questify.institution.dto.DepartmentResponse;
import com.questify.institution.exception.DuplicateResourceException;
import com.questify.institution.exception.ResourceNotFoundException;
import com.questify.institution.repository.DepartmentRepository;
import com.questify.institution.repository.InstitutionRepository;
import com.questify.institution.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;
    private final CurrentUserProvider currentUserProvider;

    public DepartmentService(DepartmentRepository departmentRepository,
            InstitutionRepository institutionRepository,
            CurrentUserProvider currentUserProvider) {
        this.departmentRepository = departmentRepository;
        this.institutionRepository = institutionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public Page<DepartmentResponse> list(Long institutionId, Pageable pageable) {
        Long scoped = currentUserProvider.resolveInstitutionId(institutionId);
        return departmentRepository.findByInstitutionId(scoped, pageable).map(DepartmentResponse::from);
    }

    public List<DepartmentResponse> listActive(Long institutionId) {
        Long scoped = currentUserProvider.resolveInstitutionId(institutionId);
        return departmentRepository.findByInstitutionIdAndActiveTrue(scoped).stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse get(Long id) {
        return DepartmentResponse.from(getEntity(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        Long institutionId = currentUserProvider.resolveInstitutionId(request.institutionId());
        requireInstitution(institutionId);
        if (departmentRepository.existsByInstitutionIdAndCodeIgnoreCase(institutionId, request.code())) {
            throw new DuplicateResourceException("A department with code '" + request.code() + "' already exists");
        }
        Department entity = new Department();
        entity.setInstitutionId(institutionId);
        apply(entity, request);
        return DepartmentResponse.from(departmentRepository.save(entity));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department entity = getEntity(id);
        if (!entity.getCode().equalsIgnoreCase(request.code())
                && departmentRepository.existsByInstitutionIdAndCodeIgnoreCase(entity.getInstitutionId(),
                        request.code())) {
            throw new DuplicateResourceException("A department with code '" + request.code() + "' already exists");
        }
        apply(entity, request);
        return DepartmentResponse.from(departmentRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.delete(getEntity(id));
    }

    private Department getEntity(Long id) {
        Department entity = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        currentUserProvider.assertCanAccess(entity.getInstitutionId());
        return entity;
    }

    private void requireInstitution(Long institutionId) {
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", institutionId);
        }
    }

    private void apply(Department entity, DepartmentRequest request) {
        entity.setName(request.name().trim());
        entity.setCode(request.code().trim().toUpperCase());
        entity.setHeadName(request.headName());
        entity.setHeadEmail(request.headEmail());
        entity.setDescription(request.description());
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }
}
