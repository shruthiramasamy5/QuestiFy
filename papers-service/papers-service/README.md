# QuestiFy - papers-service

Owns **finalized** question papers and their evaluation schemes.

| Item | Value |
|------|-------|
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.papers` |
| Port | 8085 |
| Database | MySQL `papers_service` |
| Discovery | Eureka |
| Frontend route | `/papers` |

## Entities

- **Paper** - finalized paper. Holds `generatedDraftId`, a *reference* to the draft owned by
  `paper-generation-service`. This service never touches that service's database; it reads the
  draft over HTTP through Eureka (`PAPER-GENERATION-SERVICE`).
- **EvaluationScheme** - marks distribution, per-question rubrics (`QuestionRubric`) and
  evaluation configuration (negative marking, partial marking, JSON config).

## Endpoints

| Method | Path | Roles |
|--------|------|-------|
| GET | `/api/papers` (`?subjectCode=`, `?status=`) | FACULTY, COURSE_COORDINATOR, HOD, REVIEWER |
| GET | `/api/papers/{id}` | FACULTY, COURSE_COORDINATOR, HOD, REVIEWER |
| POST | `/api/papers` | FACULTY, COURSE_COORDINATOR |
| POST | `/api/papers/from-draft` | FACULTY, COURSE_COORDINATOR |
| PUT | `/api/papers/{id}/evaluation` | FACULTY, COURSE_COORDINATOR, HOD |

### Sample: finalize a generated draft

```json
POST /api/papers/from-draft
Authorization: Bearer <questify-jwt>

{ "draftId": "6650f2c1a1b2c3d4e5f60718", "title": "CS101 Internal 1" }
```

### Sample: evaluation scheme

```json
PUT /api/papers/1/evaluation

{
  "totalMarks": 50,
  "passingMarks": 20,
  "negativeMarking": false,
  "partialMarking": true,
  "rubrics": [
    { "questionNumber": 1, "questionId": "q1", "marks": 20, "criteria": "Definition 5, derivation 15" },
    { "questionNumber": 2, "questionId": "q2", "marks": 30, "criteria": "Algorithm 15, complexity 15" }
  ]
}
```

Validation rules: scheme total must equal the paper total, rubric marks must sum to the total,
question numbers must be unique, passing marks cannot exceed the total.

## Security

Reuses the shared QuestiFy JWT (HS256, `questify.jwt.secret`, roles in the `roles` claim).
`JwtAuthenticationFilter` + `@PreAuthorize` guard every endpoint; `GlobalExceptionHandler`
returns a consistent `ApiError` body.

## API Gateway routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: papers-service
          uri: lb://PAPERS-SERVICE
          predicates:
            - Path=/api/papers/**
```

## Configuration

| Property | Env var | Default |
|----------|---------|---------|
| `spring.datasource.url` | `MYSQL_URL` | `jdbc:mysql://localhost:3306/papers_service?createDatabaseIfNotExist=true` |
| `spring.datasource.username` | `MYSQL_USER` | `root` |
| `spring.datasource.password` | `MYSQL_PASSWORD` | `root` |
| `eureka.client.service-url.defaultZone` | `EUREKA_SERVER_URL` | `http://localhost:8761/eureka/` |
| `questify.jwt.secret` | `JWT_SECRET` | development secret |

## Build and run

```bash
mvn clean install
mvn spring-boot:run
```

Tests use in-memory H2, so no MySQL is required:

```bash
mvn clean test
```
