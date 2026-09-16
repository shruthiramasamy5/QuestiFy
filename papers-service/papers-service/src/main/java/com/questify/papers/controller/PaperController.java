package com.questify.papers.controller;

import com.questify.papers.dto.CreatePaperFromDraftRequest;
import com.questify.papers.dto.CreatePaperRequest;
import com.questify.papers.dto.EvaluationSchemeRequest;
import com.questify.papers.dto.EvaluationSchemeResponse;
import com.questify.papers.dto.PaperResponse;
import com.questify.papers.entity.PaperStatus;
import com.questify.papers.service.PaperService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/papers")
public class PaperController {

    private final PaperService service;

    public PaperController(PaperService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','REVIEWER','INSTITUTION_ADMIN','SUPER_ADMIN')")
    public List<PaperResponse> list(@RequestParam(required = false) String subjectCode,
                                    @RequestParam(required = false) PaperStatus status) {
        return service.findAll(subjectCode, status).stream().map(PaperResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','REVIEWER','INSTITUTION_ADMIN','SUPER_ADMIN')")
    public PaperResponse getById(@PathVariable Long id) {
        return PaperResponse.from(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
    public ResponseEntity<PaperResponse> create(@Valid @RequestBody CreatePaperRequest request,
                                                Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaperResponse.from(service.create(request, user)));
    }

    @PostMapping("/from-draft")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
    public ResponseEntity<PaperResponse> createFromDraft(
            @Valid @RequestBody CreatePaperFromDraftRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaperResponse.from(service.createFromDraft(request, user, authorization)));
    }

    @PutMapping("/{id}/evaluation")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','REVIEWER')")
    public EvaluationSchemeResponse updateEvaluation(@PathVariable Long id,
                                                     @Valid @RequestBody EvaluationSchemeRequest request,
                                                     Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "system";
        return EvaluationSchemeResponse.from(service.upsertEvaluationScheme(id, request, user));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','REVIEWER','INSTITUTION_ADMIN','SUPER_ADMIN')")
    public PaperResponse updateStatus(@PathVariable Long id,
                                      @RequestParam PaperStatus status,
                                      Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "system";
        return PaperResponse.from(service.updateStatus(id, status, user));
    }
}
