# institution-service

QuestiFy microservice owning **institutions, departments, templates and plan usage**.
Sibling Maven module to `auth-service`, same conventions: Java 21, Spring Boot 4.1.0,
Spring Cloud 2025.1.2, Maven, Eureka registration, MySQL.

- Package: `com.questify.institution`
- Port: `8082`
- Database: `institution_service` (MySQL)
- Service id (Eureka): `institution-service`

## Frontend screens covered

| Screen | Endpoint |
| --- | --- |
| Institution Admin `/departments` | `/api/departments` |
| Institution Admin `/templates` | `/api/templates` |
| Institution Admin `/settings` | `GET/PUT /api/institutions/me`, `/api/institutions/{id}` |
| Institution Admin `/plan` | `GET /api/plan-usage/stats`, `GET /api/plan-usage/history` |
| Institution Admin `/activity` | `GET /api/plan-usage/history` (usage timeline; audit events live in audit-log-service) |
| Super Admin `/institutions` | `/api/institutions` (paged, searchable, status filter) |
| Super Admin `/billing` | `GET /api/plan-usage/billing-summary` |

## API

### `/api/institutions`
- `GET /api/institutions?search=&status=&page=&size=&sort=` — SUPER_ADMIN, paged
- `GET /api/institutions/me` — current caller's institution
- `GET /api/institutions/{id}`
- `POST /api/institutions` — SUPER_ADMIN
- `PUT /api/institutions/{id}` — SUPER_ADMIN / INSTITUTION_ADMIN (own)
- `PATCH /api/institutions/{id}/status` — SUPER_ADMIN
- `DELETE /api/institutions/{id}` — SUPER_ADMIN

### `/api/departments`
- `GET /api/departments?institutionId=&page=&size=&sort=`
- `GET /api/departments/active`
- `GET /api/departments/{id}`
- `POST /api/departments`, `PUT /api/departments/{id}`, `DELETE /api/departments/{id}`

### `/api/templates`
- `GET /api/templates?institutionId=&type=&page=&size=&sort=`
- `GET /api/templates/{id}`
- `POST /api/templates`, `PUT /api/templates/{id}`, `DELETE /api/templates/{id}`

### `/api/plan-usage`
- `GET /api/plan-usage/stats?institutionId=` — plan + usage statistics (limits, used, utilization %)
- `GET /api/plan-usage/history?institutionId=`
- `GET /api/plan-usage/billing-summary` — SUPER_ADMIN
- `POST /api/plan-usage/record` — SUPER_ADMIN / SERVICE, used by sibling services to report real usage

## Security

JWTs issued by `auth-service` are validated locally with the shared HMAC secret
`questify.jwt.secret` (`QUESTIFY_JWT_SECRET`). The filter reads `sub`, `email`,
`institutionId` and `roles` (also accepts a single `role` claim, with or without the
`ROLE_` prefix) and builds a stateless `AuthenticatedUser` principal.
Non-super-admin callers are hard-pinned to the `institutionId` in their own token,
so cross-tenant reads/writes are rejected with `403`.

No credentials are hardcoded: DB user/password and the JWT secret come from the
environment (see `.env.example`).

## Run locally

```bash
export DB_USERNAME=root DB_PASSWORD=yourpassword
export QUESTIFY_JWT_SECRET=<same secret as auth-service>
mvn clean install
mvn spring-boot:run
```

MySQL must be reachable and the Eureka discovery server should be running on
`http://localhost:8761/eureka/` (override with `EUREKA_SERVER_URL`).

## Tests

```bash
mvn clean test
```

Tests run against in-memory H2 with the `test` profile; Eureka is disabled.

## api-gateway route

Add the route in `docs/api-gateway-route.yml` to
`api-gateway/src/main/resources/application.yml` under `spring.cloud.gateway.routes`:

```yaml
- id: institution-service
  uri: lb://institution-service
  predicates:
    - Path=/api/institutions/**,/api/departments/**,/api/templates/**,/api/plan-usage/**
```

## Service-to-service calls

`AppConfig` exposes a `@LoadBalanced RestClient.Builder`, so any outbound call uses
Eureka logical names (`http://papers-service/...`). This service never touches another
service's database.
