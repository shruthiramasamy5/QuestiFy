# QuestiFy - audit-log-service

Append-only audit trail for the `/audit-log` and `/activity` frontend routes.

| Item | Value |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.audit` |
| Port | 8089 |
| Database | MongoDB (`audit_log_service`) |
| Discovery | Eureka client |

## Build & run

```bash
mvn clean install
mvn spring-boot:run
```

Needs MongoDB (`MONGODB_URI`, default `mongodb://localhost:27017/audit_log_service`)
and Eureka (`EUREKA_URL`). Tests need neither:

```bash
mvn clean test
```

## Document

`AuditEvent` (collection `audit_events`): `actorUserId`, `actorRole`, `action`,
`targetEntityType`, `targetEntityId`, `institutionId`, `timestamp`, `metadata`.
`metadata` is a free-form document (`Map<String, Object>`), so each action type can
attach whatever nested JSON it needs. Indexes on `actorUserId`, `action`, `timestamp`.

## API

| Method | Path | Roles | Description |
| --- | --- | --- | --- |
| POST | `/api/audit-events` | any authenticated caller (services included) | Appends one event; actor defaults to the JWT subject/role |
| GET | `/api/audit-events` | Super Admin, Institution Admin | Reads the log, newest first |

Query filters: `actor`, `action`, `from`, `to` (ISO-8601 instants), `page`, `size`
(1-200). Institution Admins are automatically scoped to their own
`institutionId`; Super Admins see every tenant.

### Append-only guarantee

There is no `PUT`, `PATCH` or `DELETE` endpoint and the service never calls
`delete`/`update` on the repository, so historical events cannot be modified
through the API. For defence in depth, grant the MongoDB user only
`insert`/`find` on `audit_events` in production.

JWT validation is the shared QuestiFy implementation
(`Authorization: Bearer <jwt>`, `questify.jwt.secret`, `roles` claim).

## API Gateway routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: audit-log-service
          uri: lb://AUDIT-LOG-SERVICE
          predicates:
            - Path=/api/audit-events/**
```
