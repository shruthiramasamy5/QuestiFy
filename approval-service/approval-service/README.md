# QuestiFy - approval-service

Paper approval workflow microservice (`FACULTY -> HOD -> REVIEWER`).

| Item | Value |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.approval` |
| Port | 8086 |
| Database | MySQL `approval_service` |
| Discovery | Eureka client |

## Build & run

```bash
mvn clean install
mvn spring-boot:run
```

Requires MySQL on `localhost:3306` and a Eureka server on `localhost:8761`
(override with `MYSQL_HOST`, `MYSQL_USER`, `MYSQL_PASSWORD`, `EUREKA_URL`).
Schema is created automatically (`ddl-auto: update`).

Tests need no infrastructure:

```bash
mvn clean test
```

## Security

Every request must carry the QuestiFy JWT issued by auth-service:

```
Authorization: Bearer <jwt>
```

The token is validated locally with the shared HMAC secret
(`questify.jwt.secret` / env `QUESTIFY_JWT_SECRET`) — the same validation used by
the other QuestiFy services. Roles are read from the `roles` claim and mapped to
`ROLE_*` authorities.

## API

| Method | Path | Roles | Notes |
| --- | --- | --- | --- |
| GET | `/api/approvals?status=PENDING` | HOD, Reviewer, Faculty | Queue filtered by the caller's role and the request's current stage |
| GET | `/api/approvals/{id}` | HOD, Reviewer, owner Faculty | Full history with steps |
| POST | `/api/approvals` | Faculty, HOD | Opens a workflow for a paper |
| POST | `/api/approvals/{id}/approve` | HOD or Reviewer (stage must match) | Advances the workflow |
| POST | `/api/approvals/{id}/reject` | HOD or Reviewer (stage must match) | Terminates as REJECTED |

Authorization rules enforced in `ApprovalWorkflowService`:

* A user can only action a request whose `currentStage` matches one of their roles
  (a Reviewer cannot approve while the paper is still at the HOD stage) — otherwise `403`.
* Already `APPROVED` / `REJECTED` requests cannot be actioned again — `409`.
* Faculty see only their own submissions.

Frontend routes served by this API: `/approval-queue` (`status=PENDING`) and
`/approved` (`status=APPROVED`).

## API Gateway routing

Add to the gateway `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: approval-service
          uri: lb://APPROVAL-SERVICE
          predicates:
            - Path=/api/approvals/**
```
