package com.questify.institution.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.questify.institution.dto.BillingSummaryResponse;
import com.questify.institution.dto.PlanUsageResponse;
import com.questify.institution.dto.PlanUsageStatsResponse;
import com.questify.institution.dto.UsageRecordRequest;
import com.questify.institution.service.PlanUsageService;

import jakarta.validation.Valid;

/** Backs Institution Admin -> /plan and Super Admin -> /billing */
@RestController
@RequestMapping("/api/plan-usage")
public class PlanUsageController {

    private final PlanUsageService planUsageService;

    public PlanUsageController(PlanUsageService planUsageService) {
        this.planUsageService = planUsageService;
    }

    /** Plan + usage statistics for the caller's institution (or a target one for super admins). */
    @GetMapping("/stats")
    public PlanUsageStatsResponse stats(@RequestParam(required = false) Long institutionId) {
        return planUsageService.stats(institutionId);
    }

    @GetMapping("/history")
    public List<PlanUsageResponse> history(@RequestParam(required = false) Long institutionId) {
        return planUsageService.history(institutionId);
    }

    @GetMapping("/billing-summary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<BillingSummaryResponse> billingSummary() {
        return planUsageService.billingSummary();
    }

    /** Called by sibling services (discovered through Eureka) to report real usage. */
    @PostMapping("/record")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SERVICE')")
    public PlanUsageResponse record(@Valid @RequestBody UsageRecordRequest request) {
        return planUsageService.record(request);
    }
}
