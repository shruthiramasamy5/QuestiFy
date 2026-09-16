package com.questify.mlhealth.service;

import com.questify.mlhealth.config.MlProperties;
import com.questify.mlhealth.dto.MlHealthResponse;
import com.questify.mlhealth.dto.MlProbeResult;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Periodically probes the configured external AI/ML endpoint and keeps status,
 * latency, last-checked time and the error count/rate in memory.
 */
@Service
public class MlHealthMonitor {

    private static final Logger log = LoggerFactory.getLogger(MlHealthMonitor.class);

    private final RestClient restClient;
    private final MlProperties properties;
    private final Clock clock;

    private final AtomicLong totalChecks = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();
    private final Deque<MlProbeResult> window = new ArrayDeque<>();

    private volatile MlProbeResult last;

    public MlHealthMonitor(RestClient restClient, MlProperties properties, Clock clock) {
        this.restClient = restClient;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${questify.ml.probe-interval:60s}", initialDelayString = "PT2S")
    public void scheduledProbe() {
        probe();
    }

    /** Executes one probe and records the result. */
    public synchronized MlProbeResult probe() {
        Instant startedAt = clock.instant();
        long startNanos = System.nanoTime();
        MlProbeResult result;
        try {
            var response = restClient.get()
                    .uri(properties.getEndpoint())
                    .retrieve()
                    .toBodilessEntity();
            long latency = elapsedMs(startNanos);
            int status = response.getStatusCode().value();
            result = response.getStatusCode().is2xxSuccessful()
                    ? MlProbeResult.ok(status, latency, startedAt)
                    : MlProbeResult.failure(status, latency, startedAt, "HTTP " + status);
        } catch (RuntimeException ex) {
            result = MlProbeResult.failure(null, elapsedMs(startNanos), startedAt,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
            log.warn("ML endpoint {} probe failed: {}", properties.getEndpoint(), ex.getMessage());
        }
        record(result);
        return result;
    }

    private void record(MlProbeResult result) {
        last = result;
        totalChecks.incrementAndGet();
        if (!result.success()) {
            errorCount.incrementAndGet();
        }
        window.addLast(result);
        while (window.size() > Math.max(1, properties.getWindowSize())) {
            window.removeFirst();
        }
    }

    private long elapsedMs(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
    }

    public synchronized MlHealthResponse currentHealth() {
        MlProbeResult snapshot = last;
        long windowErrors = window.stream().filter(result -> !result.success()).count();
        double errorRate = window.isEmpty() ? 0.0
                : Math.round(windowErrors * 10000.0 / window.size()) / 100.0;
        Double averageLatency = window.isEmpty() ? null
                : Math.round(window.stream().mapToLong(MlProbeResult::latencyMs).average().orElse(0) * 100.0)
                        / 100.0;
        String status = snapshot == null ? "UNKNOWN" : snapshot.success() ? "UP" : "DOWN";
        return new MlHealthResponse(properties.getEndpoint(), status,
                snapshot == null ? null : snapshot.httpStatus(),
                snapshot == null ? null : snapshot.latencyMs(),
                averageLatency,
                snapshot == null ? null : snapshot.checkedAt(),
                totalChecks.get(), errorCount.get(), errorRate,
                snapshot == null ? null : snapshot.error());
    }
}
