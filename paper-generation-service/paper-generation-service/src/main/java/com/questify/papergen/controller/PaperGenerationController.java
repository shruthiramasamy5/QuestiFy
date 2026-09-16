package com.questify.papergen.controller;

import com.questify.papergen.dto.GeneratePaperRequest;
import com.questify.papergen.dto.GeneratedPaperResponse;
import com.questify.papergen.service.PaperGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generate")
public class PaperGenerationController {

    private final PaperGenerationService service;

    public PaperGenerationController(PaperGenerationService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR')")
    public ResponseEntity<GeneratedPaperResponse> generate(
            @Valid @RequestBody GeneratePaperRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : "system";
        GeneratedPaperResponse body = GeneratedPaperResponse.from(
                service.generate(request, username, authorization));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','REVIEWER')")
    public GeneratedPaperResponse getById(@PathVariable String id) {
        return GeneratedPaperResponse.from(service.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','REVIEWER')")
    public List<GeneratedPaperResponse> list(@RequestParam(required = false) String subjectCode) {
        return service.findAll(subjectCode).stream().map(GeneratedPaperResponse::from).toList();
    }
}
