package com.questify.backup.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateBackupSetRequest(@Size(max = 200) String label,
                                     @NotEmpty(message = "must contain at least one paper id")
                                     List<Long> paperIds,
                                     @Size(max = 500) String storageReference) {
}
