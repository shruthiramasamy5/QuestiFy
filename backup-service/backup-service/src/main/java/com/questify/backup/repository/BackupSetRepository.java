package com.questify.backup.repository;

import com.questify.backup.domain.BackupSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackupSetRepository extends JpaRepository<BackupSet, Long> {

    List<BackupSet> findAllByOrderByCreatedAtDesc();

    List<BackupSet> findByInstitutionIdOrderByCreatedAtDesc(String institutionId);
}
