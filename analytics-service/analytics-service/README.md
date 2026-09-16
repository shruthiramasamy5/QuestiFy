# QuestiFy - analytics-service

Aggregation/reporting microservice for the `/analytics` frontend route.

| Item | Value |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.analytics` |
| Port | 8088 |
| Database | MySQL `analytics_service` (reserved for future materialised views/caches) |
| Discovery | Eureka client + load-balanced `WebClient` |

## Build & run

```bash
mvn clean install
mvn spring-boot:run
```

```bash
mvn clean test
```

## How data is collected

All numbers are pulled over HTTP through Eureka service discovery — **no upstream
database is ever accessed directly**:

| Upstream (Eureka id) | Endpoint used |
| --- | --- |
| `QUESTION-BANK-SERVICE` | `GET /api/questions` |
| `PAPER-GENERATION-SERVICE` | `GET /api/generated-papers` |
| `PAPERS-SERVICE` | `GET /api/papers` |
| `APPROVAL-SERVICE` | `GET /api/approvals` |

The caller's JWT is forwarded to each upstream call. If an upstream is down the
dashboard still renders and the service id is listed in `degradedSources`.
Service ids and the timeout are configurable under `questify.analytics.*`.

## Statistics produced

* papers generated per month (`papersPerMonth`)
* total question count
* CO coverage (share of questions per course outcome)
* Bloom coverage (share of questions per Bloom level)
* approval counts and average approval turnaround in hours

## API (roles: Faculty, HOD)

| Method | Path | Roles | Scope |
| --- | --- | --- | --- |
| GET | `/api/analytics` | Faculty, HOD | Faculty: own rows (`scope=FACULTY`); HOD: department-wide (`scope=DEPARTMENT`) |
| GET | `/api/analytics/questions` | Faculty, HOD | Question counts + CO/Bloom coverage |
| GET | `/api/analytics/papers` | Faculty, HOD | Paper totals + per-month series |
| GET | `/api/analytics/approvals` | HOD only | Approval counts + turnaround |

Any other role receives `403`. JWT validation is the shared QuestiFy
implementation (`Authorization: Bearer <jwt>`, `questify.jwt.secret`).

## API Gateway routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: analytics-service
          uri: lb://ANALYTICS-SERVICE
          predicates:
            - Path=/api/analytics/**
```
