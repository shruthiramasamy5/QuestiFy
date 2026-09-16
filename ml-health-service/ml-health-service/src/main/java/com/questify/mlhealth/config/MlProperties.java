package com.questify.mlhealth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of the EXTERNAL AI/ML service that is probed. This service never
 * implements or fakes the model itself — the URL points at a third-party system.
 */
@ConfigurationProperties(prefix = "questify.ml")
public class MlProperties {

    /** Health/ping URL of the external ML service, e.g. https://ml.example.com/health. */
    private String endpoint = "http://localhost:9000/health";

    /** Per-probe timeout. */
    private Duration timeout = Duration.ofSeconds(5);

    /** How often the scheduler probes the endpoint. */
    private Duration probeInterval = Duration.ofSeconds(60);

    /** Number of probe results kept for the error-rate window. */
    private int windowSize = 20;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getProbeInterval() {
        return probeInterval;
    }

    public void setProbeInterval(Duration probeInterval) {
        this.probeInterval = probeInterval;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }
}
