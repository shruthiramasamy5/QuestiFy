package com.questify.backup.service;

import com.questify.backup.config.BackupProperties;
import com.questify.backup.domain.BackupSet;
import com.questify.backup.dto.BackupSetResponse;
import com.questify.backup.dto.CreateBackupSetRequest;
import com.questify.backup.repository.BackupSetRepository;
import com.questify.backup.security.AuthenticatedUser;
import com.questify.backup.web.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupSetService {

    private final BackupSetRepository repository;
    private final BackupProperties properties;

    public BackupSetService(BackupSetRepository repository, BackupProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<BackupSetResponse> list(AuthenticatedUser user) {
        List<BackupSet> sets = user.institutionId() == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByInstitutionIdOrderByCreatedAtDesc(user.institutionId());
        return sets.stream().map(BackupSetResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BackupSetResponse get(AuthenticatedUser user, Long id) {
        BackupSet set = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Backup set " + id + " not found"));
        if (user.institutionId() != null && set.getInstitutionId() != null
                && !user.institutionId().equals(set.getInstitutionId())) {
            throw new AccessDeniedException("Backup set " + id + " belongs to another institution");
        }
        return BackupSetResponse.from(set);
    }

    @Transactional
    public BackupSetResponse create(AuthenticatedUser user, CreateBackupSetRequest payload) {
        List<Long> paperIds = payload.paperIds().stream().distinct().toList();
        if (paperIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("paperIds must contain positive identifiers");
        }
        BackupSet set = new BackupSet();
        set.setLabel(payload.label() == null || payload.label().isBlank()
                ? "Backup " + Instant.now() : payload.label().trim());
        set.setPaperIds(paperIds);
        set.setCreatedBy(user.userId());
        set.setInstitutionId(user.institutionId());
        set.setCreatedAt(Instant.now());
        set.setStorageReference(payload.storageReference() == null || payload.storageReference().isBlank()
                ? defaultStorageReference()
                : payload.storageReference().trim());
        return BackupSetResponse.from(repository.save(set));
    }

    private String defaultStorageReference() {
        return "%s/%s.zip".formatted(properties.getStoragePrefix(), UUID.randomUUID());
    }
}
