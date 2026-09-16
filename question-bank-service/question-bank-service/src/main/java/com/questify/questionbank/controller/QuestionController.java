package com.questify.questionbank.controller;

import com.questify.questionbank.dto.PageResponse;
import com.questify.questionbank.dto.QuestionRequest;
import com.questify.questionbank.dto.QuestionResponse;
import com.questify.questionbank.dto.QuestionSearchCriteria;
import com.questify.questionbank.entity.BloomLevel;
import com.questify.questionbank.entity.Difficulty;
import com.questify.questionbank.entity.QuestionType;
import com.questify.questionbank.service.QuestionService;
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
@RequestMapping("/api/questions")
@PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','INSTITUTION_ADMIN','SUPER_ADMIN','REVIEWER')")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** Paginated search used by /question-bank and paper-generation-service. */
    @GetMapping
    public PageResponse<QuestionResponse> search(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) Long courseOutcomeId,
            @RequestParam(required = false) String courseOutcomeCode,
            @RequestParam(required = false) BloomLevel bloomLevel,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) QuestionType questionType,
            @RequestParam(required = false) Integer marks,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String effectiveSubject = subject != null && !subject.isBlank() ? subject : subjectCode;
        QuestionSearchCriteria criteria = new QuestionSearchCriteria(effectiveSubject, unit, courseOutcomeId,
                courseOutcomeCode, bloomLevel, difficulty, questionType, marks, search);
        return questionService.search(criteria, pageable);
    }

    @GetMapping("/{id}")
    public QuestionResponse get(@PathVariable Long id) {
        return questionService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','INSTITUTION_ADMIN')")
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.create(request));
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','INSTITUTION_ADMIN')")
    public ResponseEntity<com.questify.questionbank.dto.BulkUploadResponse> bulkUpload(
            @RequestBody java.util.List<QuestionRequest> requests) {
        return ResponseEntity.ok(questionService.bulkUpload(requests));
    }

    @PostMapping("/syllabus-upload")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','INSTITUTION_ADMIN')")
    public ResponseEntity<com.questify.questionbank.dto.SyllabusUploadResponse> syllabusUpload(
            @RequestBody com.questify.questionbank.dto.SyllabusUploadRequest request) {
        return ResponseEntity.ok(questionService.syllabusUpload(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','INSTITUTION_ADMIN')")
    public QuestionResponse update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','INSTITUTION_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
