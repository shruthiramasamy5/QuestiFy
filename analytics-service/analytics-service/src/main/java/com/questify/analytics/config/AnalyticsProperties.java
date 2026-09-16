package com.questify.analytics.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Logical Eureka service ids of the upstream services this service aggregates. */
@ConfigurationProperties(prefix = "questify.analytics")
public class AnalyticsProperties {

    private String questionBankService = "QUESTION-BANK-SERVICE";
    private String paperGenerationService = "PAPER-GENERATION-SERVICE";
    private String papersService = "PAPERS-SERVICE";
    private String approvalService = "APPROVAL-SERVICE";
    private Duration timeout = Duration.ofSeconds(5);

    public String getQuestionBankService() {
        return questionBankService;
    }

    public void setQuestionBankService(String questionBankService) {
        this.questionBankService = questionBankService;
    }

    public String getPaperGenerationService() {
        return paperGenerationService;
    }

    public void setPaperGenerationService(String paperGenerationService) {
        this.paperGenerationService = paperGenerationService;
    }

    public String getPapersService() {
        return papersService;
    }

    public void setPapersService(String papersService) {
        this.papersService = papersService;
    }

    public String getApprovalService() {
        return approvalService;
    }

    public void setApprovalService(String approvalService) {
        this.approvalService = approvalService;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
