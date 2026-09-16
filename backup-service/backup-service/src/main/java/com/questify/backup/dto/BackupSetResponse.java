package com.questify.backup.dto;

import com.questify.backup.domain.BackupSet;
import java.time.Instant;
import java.util.List;

public record BackupSetResponse(Long id, String label, List<Long> paperIds, int paperCount,
                                String createdBy, Instant createdAt, String storageReference) {

    public static BackupSetResponse from(BackupSet set) {
        List<Long> paperIds = List.copyOf(set.getPaperIds());
        return new BackupSetResponse(set.getId(), set.getLabel(), paperIds, paperIds.size(),
                set.getCreatedBy(), set.getCreatedAt(), set.getStorageReference());
    }
}
