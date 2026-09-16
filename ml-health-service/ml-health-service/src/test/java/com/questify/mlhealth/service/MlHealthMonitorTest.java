package com.questify.mlhealth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.questify.mlhealth.config.MlProperties;
import com.questify.mlhealth.dto.MlHealthResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class MlHealthMonitorTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), ZoneOffset.UTC);

    private MlProperties properties(String endpoint) {
        MlProperties properties = new MlProperties();
        properties.setEndpoint(endpoint);
        properties.setTimeout(Duration.ofSeconds(2));
        properties.setWindowSize(5);
        return properties;
    }

    @Test
    void statusIsUnknownBeforeFirstProbe() {
        MlHealthMonitor monitor = new MlHealthMonitor(RestClient.create(),
                properties("http://localhost:1/health"), clock);

        MlHealthResponse health = monitor.currentHealth();

        assertThat(health.status()).isEqualTo("UNKNOWN");
        assertThat(health.totalChecks()).isZero();
        assertThat(health.lastCheckedAt()).isNull();
        assertThat(health.errorRate()).isZero();
    }

    @Test
    void successfulProbeReportsUpWithLatency() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "{\"status\":\"ok\"}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String endpoint = "http://localhost:" + server.getAddress().getPort() + "/health";
            MlHealthMonitor monitor =
                    new MlHealthMonitor(RestClient.create(), properties(endpoint), clock);

            monitor.probe();
            MlHealthResponse health = monitor.currentHealth();

            assertThat(health.status()).isEqualTo("UP");
            assertThat(health.lastHttpStatus()).isEqualTo(200);
            assertThat(health.latencyMs()).isNotNull();
            assertThat(health.errorCount()).isZero();
            assertThat(health.errorRate()).isZero();
            assertThat(health.lastCheckedAt()).isEqualTo(Instant.parse("2026-07-01T12:00:00Z"));
            assertThat(health.endpoint()).isEqualTo(endpoint);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void unreachableEndpointReportsDownAndCountsErrors() {
        MlHealthMonitor monitor = new MlHealthMonitor(RestClient.create(),
                properties("http://localhost:1/health"), clock);

        monitor.probe();
        monitor.probe();
        MlHealthResponse health = monitor.currentHealth();

        assertThat(health.status()).isEqualTo("DOWN");
        assertThat(health.totalChecks()).isEqualTo(2);
        assertThat(health.errorCount()).isEqualTo(2);
        assertThat(health.errorRate()).isEqualTo(100.0);
        assertThat(health.lastError()).isNotBlank();
    }

    @Test
    void serverErrorIsTreatedAsFailure() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/health", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();
        try {
            MlHealthMonitor monitor = new MlHealthMonitor(RestClient.create(),
                    properties("http://localhost:" + server.getAddress().getPort() + "/health"), clock);

            monitor.probe();
            MlHealthResponse health = monitor.currentHealth();

            assertThat(health.status()).isEqualTo("DOWN");
            assertThat(health.errorCount()).isEqualTo(1);
        } finally {
            server.stop(0);
        }
    }
}
