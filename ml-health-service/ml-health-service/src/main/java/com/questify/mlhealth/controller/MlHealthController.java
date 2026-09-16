package com.questify.mlhealth.controller;

import com.questify.mlhealth.dto.MlHealthResponse;
import com.questify.mlhealth.service.MlHealthMonitor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Backs the frontend route /ml-health. Super Admin only. */
@RestController
@RequestMapping("/api/ml-health")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTION_ADMIN')")
public class MlHealthController {

    private final MlHealthMonitor monitor;

    public MlHealthController(MlHealthMonitor monitor) {
        this.monitor = monitor;
    }

    @GetMapping({"", "/overview"})
    public MlHealthResponse health() {
        return monitor.currentHealth();
    }

    /** Forces an immediate probe (the "Check now" button). */
    @PostMapping("/probe")
    public MlHealthResponse probeNow() {
        monitor.probe();
        return monitor.currentHealth();
    }
}
