package com.questify.analytics.controller;

import com.questify.analytics.dto.AnalyticsOverview;
import com.questify.analytics.dto.ApprovalStats;
import com.questify.analytics.dto.PaperStats;
import com.questify.analytics.dto.QuestionStats;
import com.questify.analytics.security.CurrentUser;
import com.questify.analytics.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Backs the frontend route /analytics. */
@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('FACULTY','COURSE_COORDINATOR','HOD','INSTITUTION_ADMIN','REVIEWER','SUPER_ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public AnalyticsOverview overview() {
        return analyticsService.overview(CurrentUser.require(), token());
    }

    @GetMapping("/questions")
    public QuestionStats questions() {
        return analyticsService.questions(CurrentUser.require(), token());
    }

    @GetMapping("/papers")
    public PaperStats papers() {
        return analyticsService.papers(CurrentUser.require(), token());
    }

    @GetMapping("/approvals")
    @PreAuthorize("hasAnyRole('HOD','REVIEWER','COURSE_COORDINATOR','INSTITUTION_ADMIN','SUPER_ADMIN')")
    public ApprovalStats approvals() {
        return analyticsService.approvals(CurrentUser.require(), token());
    }

    /** The caller's JWT is forwarded to the upstream services. */
    private String token() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "" : String.valueOf(authentication.getCredentials());
    }
}
