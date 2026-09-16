# question-bank-service

QuestiFy microservice that owns the **question bank** and **CO mapping** domains.
It is an independent Maven project and a sibling module of `auth-service`.

| Item | Value |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.questionbank` |
| Port | 8083 |
| Database | MySQL `question_bank_service` |
| Discovery | Eureka client |

## Frontend routes served

- `/question-bank` -> `/api/questions`
- `/co-mapping` -> `/api/co-mapping`

## Running

```bash
mvn clean install
mvn spring-boot:run
```

Environment overrides (all have local defaults):

```
DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD
JWT_SECRET        # MUST match auth-service
JWT_ROLES_CLAIM   # default: roles
EUREKA_URI        # default: http://localhost:8761/eureka
```

MySQL must be reachable; the schema `question_bank_service` is created
automatically (`createDatabaseIfNotExist=true`) and Hibernate manages the tables.
No mock/demo data is inserted — all data is persisted in MySQL.

## Security

Reuses the shared JWT validation pattern from `auth-service`: the bearer token
issued by auth-service is validated locally with the same HMAC secret
(`questify.jwt.secret`), and the `roles` claim becomes Spring Security
authorities (`ROLE_FACULTY`, `ROLE_COURSE_COORDINATOR`, `ROLE_HOD`).

| Operation | Roles |
| --- | --- |
| Search / read questions & COs | FACULTY, COURSE_COORDINATOR, HOD |
| Create / update questions | FACULTY, COURSE_COORDINATOR |
| Delete question | FACULTY (own), COURSE_COORDINATOR, HOD |
| Create / update / delete course outcome | COURSE_COORDINATOR, HOD |
| Map / unmap question to CO | FACULTY, COURSE_COORDINATOR |

Faculty may only modify questions they created; coordinators and HODs may modify any.

## Data model

- `Question` — question text, subject, unit, difficulty, question type, marks,
  Bloom level, created by, created/updated timestamps.
- `CourseOutcome` — code (CO1..), description, subject, optional Bloom level,
  created by, timestamps.
- `QuestionCourseOutcome` — question ↔ course outcome mapping (unique pair).
- `BloomLevel` — `K1, K2, K3, K4, K5, K6`.
- `Difficulty` — `EASY, MEDIUM, HARD`.
- `QuestionType` — `MCQ, SHORT_ANSWER, LONG_ANSWER, NUMERICAL, TRUE_FALSE`.

## API

### Questions — `/api/questions`

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/questions` | Paginated search / filtering |
| GET | `/api/questions/{id}` | Single question |
| POST | `/api/questions` | Create |
| PUT | `/api/questions/{id}` | Update |
| DELETE | `/api/questions/{id}` | Delete |

Search parameters (all optional, combinable) — this is the API that
`paper-generation-service` will consume:

`subject`, `unit`, `courseOutcomeId`, `courseOutcomeCode`, `bloomLevel`,
`difficulty`, `questionType`, `marks`, `search` (text contains),
plus `page`, `size`, `sort` (default `createdAt,desc`).

```
GET /api/questions?subject=Data%20Structures&bloomLevel=K3&difficulty=MEDIUM&courseOutcomeCode=CO1&page=0&size=20
```

Response is a page envelope: `content`, `page`, `size`, `totalElements`,
`totalPages`, `last`.

### CO mapping — `/api/co-mapping`

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/co-mapping/course-outcomes?subject=` | Paginated CO list |
| POST | `/api/co-mapping/course-outcomes` | Create CO |
| PUT | `/api/co-mapping/course-outcomes/{id}` | Update CO |
| DELETE | `/api/co-mapping/course-outcomes/{id}` | Delete CO (blocked if mapped) |
| GET | `/api/co-mapping/questions/{questionId}` | COs mapped to a question |
| POST | `/api/co-mapping` | Map question -> COs |
| DELETE | `/api/co-mapping/questions/{questionId}/course-outcomes/{coId}` | Unmap |

### Errors

All failures return a consistent body:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "Validation failed", "path": "/api/questions",
  "fieldErrors": { "marks": "must be greater than or equal to 1" } }
```

## API Gateway route

Add to the gateway configuration (see `docs/api-gateway-route.yml`):

```yaml
- id: question-bank-service
  uri: lb://question-bank-service
  predicates:
    - Path=/api/questions/**,/api/co-mapping/**
```

## Tests

```bash
mvn clean test
```

Integration tests run against in-memory H2 (`src/test/resources/application-test.yml`)
and cover authentication, role authorization, validation and filtered search.

## Project layout

```
question-bank-service/
├── pom.xml
├── README.md
├── docs/api-gateway-route.yml
└── src
    ├── main/java/com/questify/questionbank/{config,controller,dto,entity,exception,repository,security,service,specification}
    ├── main/resources/application.yml
    └── test/java/com/questify/questionbank/QuestionBankApiTest.java
```
