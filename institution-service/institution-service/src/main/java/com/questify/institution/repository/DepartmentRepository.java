package com.questify.institution.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.questify.institution.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Page<Department> findByInstitutionId(Long institutionId, Pageable pageable);

    List<Department> findByInstitutionIdAndActiveTrue(Long institutionId);

    Optional<Department> findByIdAndInstitutionId(Long id, Long institutionId);

    boolean existsByInstitutionIdAndCodeIgnoreCase(Long institutionId, String code);

    long countByInstitutionId(Long institutionId);

    void deleteByInstitutionId(Long institutionId);
}
