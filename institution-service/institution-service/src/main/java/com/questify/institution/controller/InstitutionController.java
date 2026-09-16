package com.questify.institution.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.questify.institution.domain.InstitutionStatus;
import com.questify.institution.dto.InstitutionRequest;
import com.questify.institution.dto.InstitutionResponse;
import com.questify.institution.dto.StatusUpdateRequest;
import com.questify.institution.service.InstitutionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    /** Super Admin -> /institutions */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<InstitutionResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) InstitutionStatus status,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return institutionService.list(search, status, pageable);
    }

    /** Institution Admin -> /settings (own institution profile) */
    @GetMapping("/me")
    public InstitutionResponse current() {
        return institutionService.getCurrent();
    }

    @GetMapping("/{id}")
    public InstitutionResponse get(@PathVariable Long id) {
        return institutionService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionResponse create(@Valid @RequestBody InstitutionRequest request) {
        return institutionService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTION_ADMIN')")
    public InstitutionResponse update(@PathVariable Long id, @Valid @RequestBody InstitutionRequest request) {
        return institutionService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public InstitutionResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return institutionService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
