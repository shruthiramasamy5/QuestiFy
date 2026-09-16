package com.questify.institution.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.questify.institution.domain.Template;
import com.questify.institution.domain.TemplateType;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Page<Template> findByInstitutionId(Long institutionId, Pageable pageable);

    Page<Template> findByInstitutionIdAndType(Long institutionId, TemplateType type, Pageable pageable);

    Optional<Template> findByIdAndInstitutionId(Long id, Long institutionId);

    boolean existsByInstitutionIdAndNameIgnoreCase(Long institutionId, String name);

    long countByInstitutionId(Long institutionId);

    java.util.List<Template> findByInstitutionIdAndDefaultTemplateTrue(Long institutionId);

    void deleteByInstitutionId(Long institutionId);
}
