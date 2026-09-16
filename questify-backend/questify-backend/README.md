# QuestiFy Backend

Java 21 · Spring Boot 4.1.0 · Spring Cloud 2025.1.2 (Eureka + Gateway) · MySQL · Maven multi-module reactor

## Modules

| Module          | Port | Purpose                                                              |
|-----------------|------|-----------------------------------------------------------------------|
| `eureka-server` | 8761 | Service registry — every other service registers here                |
| `api-gateway`   | 8080 | Single entry point the React frontend talks to; routes `/api/auth/**` to `auth-service` |
| `auth-service`  | 8081 | Users, roles, login/logout/me — implements the frontend's auth contract exactly |

More services (question-bank, paper-generation, approval-workflow, analytics) get added as new sibling modules the same way `auth-service` was.

## Prerequisites

- JDK 21
- MySQL running locally (or update `auth-service/src/main/resources/application.yml`)
- Maven (Eclipse's bundled M2E is fine)

## Import into Eclipse

1. `File > Import > Maven > Existing Maven Projects`
2. Point at this `questify-backend` folder (the one with the parent `pom.xml`)
3. Eclipse will detect all 3 modules from the reactor `pom.xml` — select all, Finish
4. Let M2E download dependencies (needs internet access once, for the Maven repo)

## MySQL setup

Only a running MySQL server is needed — `auth-service` creates the `questify_auth`
database itself on first startup (`createDatabaseIfNotExist=true`) and Hibernate
creates/updates tables (`ddl-auto: update`).

Default credentials expected (override via env vars if different):
```
DB_USERNAME=root
DB_PASSWORD=root
```

## Run order

Start in this order (each is a normal Spring Boot app — right-click the `*Application.java` > Run As > Java Application, or `mvn spring-boot:run` in each module):

1. `eureka-server` — wait until http://localhost:8761 loads
2. `auth-service` — registers itself with Eureka, seeds 6 demo users (dev profile only)
3. `api-gateway` — routes `/api/auth/**` to `auth-service` via Eureka

Point the frontend's `.env` at the gateway:
```
VITE_API_BASE_URL=http://localhost:8080
```

## Demo logins (seeded automatically, `dev` profile only)

Password for all of them: `Questify@123`

| Role                | Email                       |
|---------------------|------------------------------|
| super-admin          | superadmin@questify.dev     |
| institution-admin    | admin@questify.dev          |
| faculty              | faculty@questify.dev        |
| hod                  | hod@questify.dev            |
| reviewer             | reviewer@questify.dev       |
| course-coordinator   | coordinator@questify.dev    |

## Quick smoke test (no frontend needed)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"faculty@questify.dev","password":"Questify@123"}'

# copy the "token" from the response, then:
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <token>"
```

## Adding the next service

1. New module folder next to `auth-service`, its own `pom.xml` with `<parent>` pointing at the root `questify-backend` pom
2. Add `<module>your-service</module>` to the root `pom.xml`
3. Add `@EnableDiscoveryClient` + Eureka client config so it registers itself
4. Add a route for it in `api-gateway/src/main/resources/application.yml`
