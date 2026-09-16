package com.questify.backup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.questify.backup.config.BackupProperties;
import com.questify.backup.domain.BackupSet;
import com.questify.backup.dto.BackupSetResponse;
import com.questify.backup.dto.CreateBackupSetRequest;
import com.questify.backup.repository.BackupSetRepository;
import com.questify.backup.security.AuthenticatedUser;
import com.questify.backup.web.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class BackupSetServiceTest {

    @Mock
    private BackupSetRepository repository;

    private BackupSetService service;

    private final AuthenticatedUser reviewer =
            new AuthenticatedUser("u-rev", "rev@questify.test", "inst-1", Set.of("REVIEWER"));

    @BeforeEach
    void setUp() {
        service = new BackupSetService(repository, new BackupProperties());
    }

    @Test
    void createGeneratesStorageReferenceAndDeduplicatesPapers() {
        when(repository.save(any(BackupSet.class))).thenAnswer(inv -> inv.getArgument(0));

        BackupSetResponse response = service.create(reviewer,
                new CreateBackupSetRequest("July archive", List.of(1L, 2L, 2L, 3L), null));

        assertThat(response.paperIds()).containsExactly(1L, 2L, 3L);
        assertThat(response.paperCount()).isEqualTo(3);
        assertThat(response.createdBy()).isEqualTo("u-rev");
        assertThat(response.storageReference()).startsWith("questify-backups/").endsWith(".zip");
    }

    @Test
    void createRejectsInvalidPaperIds() {
        assertThatThrownBy(() -> service.create(reviewer,
                new CreateBackupSetRequest("bad", List.of(0L), null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listIsScopedToInstitution() {
        BackupSet set = new BackupSet();
        set.setId(5L);
        set.setCreatedBy("u-rev");
        set.setCreatedAt(Instant.now());
        set.setStorageReference("questify-backups/a.zip");
        set.setInstitutionId("inst-1");
        when(repository.findByInstitutionIdOrderByCreatedAtDesc("inst-1")).thenReturn(List.of(set));

        assertThat(service.list(reviewer)).hasSize(1);
    }

    @Test
    void getRejectsCrossInstitutionAccess() {
        BackupSet set = new BackupSet();
        set.setId(9L);
        set.setInstitutionId("inst-999");
        when(repository.findById(9L)).thenReturn(Optional.of(set));

        assertThatThrownBy(() -> service.get(reviewer, 9L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUnknownIdThrowsNotFound() {
        when(repository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(reviewer, 404L)).isInstanceOf(NotFoundException.class);
    }
}
