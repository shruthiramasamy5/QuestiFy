package com.questify.questionbank.controller;

import com.questify.questionbank.dto.CourseOutcomeRequest;
import com.questify.questionbank.dto.CourseOutcomeResponse;
import com.questify.questionbank.dto.PageResponse;
import com.questify.questionbank.dto.QuestionCoMappingRequest;
import com.questify.questionbank.dto.QuestionCoMappingResponse;
import com.questify.questionbank.service.CoMappingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/co-mapping")
@PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
public class CoMappingController {

    private final CoMappingService coMappingService;

    public CoMappingController(CoMappingService coMappingService) {
        this.coMappingService = coMappingService;
    }

    // Get all Course Outcomes
    @GetMapping("/course-outcomes")
    public PageResponse<CourseOutcomeResponse> listOutcomes(
            @RequestParam(required = false) String subject,
            @PageableDefault(
                    size = 20,
                    sort = "code",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return coMappingService.listOutcomes(subject, pageable);
    }

    // Create Course Outcome
    @PostMapping("/course-outcomes")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
    public ResponseEntity<CourseOutcomeResponse> createOutcome(
            @Valid @RequestBody CourseOutcomeRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(coMappingService.createOutcome(request));
    }

    // Update Course Outcome
    @PutMapping("/course-outcomes/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
    public CourseOutcomeResponse updateOutcome(
            @PathVariable Long id,
            @Valid @RequestBody CourseOutcomeRequest request) {

        return coMappingService.updateOutcome(id, request);
    }

    // Delete Course Outcome
    @DeleteMapping("/course-outcomes/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD')")
    public ResponseEntity<Void> deleteOutcome(
            @PathVariable Long id) {

        coMappingService.deleteOutcome(id);

        return ResponseEntity.noContent().build();
    }

    // Get CO mappings for a question
    @GetMapping("/questions/{questionId}")
    public QuestionCoMappingResponse getMappings(
            @PathVariable Long questionId) {

        return coMappingService.getMappings(questionId);
    }

    // Map Course Outcomes to a question
    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR')")
    public QuestionCoMappingResponse map(
            @Valid @RequestBody QuestionCoMappingRequest request) {

        return coMappingService.map(request);
    }

    // Remove Course Outcome mapping from a question
    @DeleteMapping("/questions/{questionId}/course-outcomes/{courseOutcomeId}")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR')")
    public ResponseEntity<Void> unmap(
            @PathVariable Long questionId,
            @PathVariable Long courseOutcomeId) {

        coMappingService.unmap(questionId, courseOutcomeId);

        return ResponseEntity.noContent().build();
    }
}