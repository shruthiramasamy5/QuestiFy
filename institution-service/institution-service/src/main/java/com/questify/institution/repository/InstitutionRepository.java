package com.questify.institution.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.questify.institution.domain.Institution;
import com.questify.institution.domain.InstitutionStatus;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    Page<Institution> findByStatus(InstitutionStatus status, Pageable pageable);

    Page<Institution> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code,
            Pageable pageable);
}
