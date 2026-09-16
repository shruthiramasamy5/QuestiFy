# QuestiFy - ml-health-service

Monitors the **external** AI/ML endpoint used by QuestiFy and exposes its health
to the `/ml-health` frontend route. No model and no fake AI service is
implemented here — `questify.ml.endpoint` is treated as a third-party system.

| Item | Value |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.mlhealth` |
| Port | 8090 |
| Database | none |
| Discovery | Eureka client |

## Build & run

```bash
mvn clean install
mvn spring-boot:run
```

```bash
mvn clean test
```

## Configuration

```yaml
questify:
  ml:
    endpoint: ${QUESTIFY_ML_ENDPOINT:http://localhost:9000/health}
    timeout: 5s          # per-probe connect/read timeout
    probe-interval: 60s  # scheduler period
    window-size: 20      # probes kept for the error-rate / average-latency window
```

A scheduled task (`@Scheduled(fixedDelayString = "${questify.ml.probe-interval}")`)
probes the endpoint and tracks:

* `status` — `UP`, `DOWN`, or `UNKNOWN` before the first probe
* `latencyMs` and `averageLatencyMs`
* `lastCheckedAt`
* `totalChecks`, `errorCount`, `errorRate` (percentage over the window)
* `lastError` / `lastHttpStatus`

Any non-2xx response or connection failure counts as an error.

## API (role: Super Admin)

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/ml-health` | Current status snapshot |
| POST | `/api/ml-health/probe` | Forces an immediate probe and returns the snapshot |

JWT validation is the shared QuestiFy implementation
(`Authorization: Bearer <jwt>`, `questify.jwt.secret`, `roles` claim).

## API Gateway routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ml-health-service
          uri: lb://ML-HEALTH-SERVICE
          predicates:
            - Path=/api/ml-health/**
```
