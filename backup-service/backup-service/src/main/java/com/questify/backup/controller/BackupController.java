package com.questify.backup.controller;

import com.questify.backup.dto.BackupSetResponse;
import com.questify.backup.dto.CreateBackupSetRequest;
import com.questify.backup.security.CurrentUser;
import com.questify.backup.service.BackupSetService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Backs the frontend route /backups. Reviewer role only. */
@RestController
@RequestMapping("/api/backups")
@PreAuthorize("hasAnyRole('REVIEWER','SUPER_ADMIN','INSTITUTION_ADMIN','HOD')")
public class BackupController {

    private final BackupSetService backupSetService;

    public BackupController(BackupSetService backupSetService) {
        this.backupSetService = backupSetService;
    }

    @GetMapping
    public List<BackupSetResponse> list() {
        return backupSetService.list(CurrentUser.require());
    }

    @GetMapping("/{id}")
    public BackupSetResponse get(@PathVariable Long id) {
        return backupSetService.get(CurrentUser.require(), id);
    }

    @PostMapping
    public ResponseEntity<BackupSetResponse> create(@Valid @RequestBody CreateBackupSetRequest payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(backupSetService.create(CurrentUser.require(), payload));
    }
}
