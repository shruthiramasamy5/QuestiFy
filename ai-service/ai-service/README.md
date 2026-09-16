# ai-service

QuestiFy's AI integration microservice. Wraps the real Anthropic Claude API
(`POST /v1/messages`) and exposes two internal endpoints for sibling
services to call over Eureka service discovery. There is no mock/fake AI
mode: if `ANTHROPIC_API_KEY` is unset, both endpoints return
`503 Service Unavailable` rather than fabricated content, and callers are
expected to fall back to their existing non-AI logic.

## Endpoints

Both require a valid QuestiFy JWT (`Authorization: Bearer ...`) with role
`FACULTY`, `COURSE_COORDINATOR`, or `HOD` — sibling services forward the
original caller's token rather than using a separate service credential.

### `POST /api/ai/select-questions`
Called by `paper-generation-service`. Given a target question count/marks/
CO/Bloom/difficulty distribution and a pool of already-filtered candidate
questions, asks Claude to pick the subset that best satisfies the targets.
The response is validated before being returned: every selected id must
actually be one of the candidates, no duplicates, and the count must match
the request — if the model's output fails any of these checks, the call
fails loudly (`422`) instead of silently returning something wrong.

### `POST /api/ai/draft-questions`
Called by `question-bank-service`. Given a subject/unit/course outcome/
Bloom level/difficulty/marks target, asks Claude to draft 1-10 new
candidate questions for a faculty member to review, edit, or discard —
drafts never bypass human review to become real bank questions.

## Configuration

See `.env.example`. Key variables:

| Variable | Purpose |
|---|---|
| `ANTHROPIC_API_KEY` | Required for AI features to function. Never commit a real value. |
| `ANTHROPIC_MODEL` | Defaults to `claude-sonnet-5`. |
| `JWT_SECRET` | Must match `auth-service`'s secret exactly (shared HS256 signing key). |

## Health monitoring

`GET /actuator/health` is exposed and unauthenticated, intended as the
target for `ml-health-service`'s `QUESTIFY_ML_ENDPOINT` config — point it
at `http://<ai-service-host>:8091/actuator/health` so the Super Admin
"ML health" page reflects this service's real status.

## Run

```bash
mvn spring-boot:run
```

Registers with Eureka as `AI-SERVICE`, port `8091` by default.
