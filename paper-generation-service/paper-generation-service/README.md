# QuestiFy - paper-generation-service

Generates draft question papers for QuestiFy.

| Item | Value |
|------|-------|
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.papergen` |
| Port | 8084 |
| Datastore | MongoDB (`paper_generation_service`) |
| Discovery | Eureka |

## What it does

1. Accepts paper generation parameters (`POST /api/generate`).
2. Validates the request (bean validation + distribution vs. question-count rules).
3. Calls **QUESTION-BANK-SERVICE** through Eureka (load balanced `WebClient`, no hardcoded URLs).
4. Retrieves candidate questions for the subject.
5. Applies deterministic selection rules (CO distribution -> Bloom distribution -> difficulty mix -> fill).
6. Checks repetition against previously generated papers for the same subject.
7. Builds a generated paper draft with full generation metadata.
8. Persists the draft in MongoDB.

## Endpoints

| Method | Path | Roles |
|--------|------|-------|
| POST | `/api/generate` | FACULTY, COURSE_COORDINATOR |
| GET | `/api/generate/{id}` | FACULTY, COURSE_COORDINATOR, HOD, REVIEWER |
| GET | `/api/generate?subjectCode=CS101` | FACULTY, COURSE_COORDINATOR, HOD, REVIEWER |

### Sample request

```json
POST /api/generate
Authorization: Bearer <questify-jwt>

{
  "subjectCode": "CS101",
  "subjectName": "Data Structures",
  "examType": "INTERNAL_1",
  "questionCount": 5,
  "totalMarks": 50,
  "durationMinutes": 90,
  "coDistribution": { "CO1": 2, "CO2": 2 },
  "bloomDistribution": { "APPLY": 3 },
  "difficultyMix": { "EASY": 2, "MEDIUM": 2, "HARD": 1 },
  "repetitionWindowDays": 365
}
```

## Security

JWT validation is reused from the shared QuestiFy scheme: HS256 token signed with
`questify.jwt.secret`, subject = username, roles in the `roles` claim.
`JwtAuthenticationFilter` populates the security context; endpoints are guarded with
`@PreAuthorize`.

## AI

`com.questify.papergen.ai.AiPaperGenerationProvider` is a clean extension point for a future
AI provider. **No AI integration exists yet** - the default `NoopAiPaperGenerationProvider`
reports `isAvailable() == false` and throws rather than returning fabricated output.
All shipped generation is deterministic and rule-based.

## Configuration

| Property | Env var | Default |
|----------|---------|---------|
| `spring.data.mongodb.uri` | `MONGODB_URI` | `mongodb://localhost:27017/paper_generation_service` |
| `eureka.client.service-url.defaultZone` | `EUREKA_SERVER_URL` | `http://localhost:8761/eureka/` |
| `questify.jwt.secret` | `JWT_SECRET` | development secret (override in every real env) |

## API Gateway routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: paper-generation-service
          uri: lb://PAPER-GENERATION-SERVICE
          predicates:
            - Path=/api/generate/**
```

## Build and run

```bash
mvn clean install
mvn spring-boot:run
```

Tests (no MongoDB or Eureka required - they are pure unit tests):

```bash
mvn clean test
```
