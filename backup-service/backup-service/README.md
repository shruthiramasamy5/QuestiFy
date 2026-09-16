# QuestiFy - backup-service

Registry of backup sets (paper snapshots). **Metadata only** — actually writing
archive files to disk / object storage is out of scope; `storageReference` is the
agreed pointer for whatever job performs the copy.

## Architecture note (why it is a separate service)

The domain is small (one entity, three endpoints) and it would be simpler to keep
`BackupSet` inside `approval-service`: one less deployment, one less MySQL schema,
no cross-service call, and reviewers already authenticate against that service.
The trade-offs that justify the split:

* backup sets have a different lifecycle and retention policy than approvals and
  will grow into real storage/restore work (object storage, scheduled jobs),
* it keeps `approval_service` focused on the workflow tables,
* it can be scaled or taken offline independently ("this service is optional").

Delivered here as a standalone service as requested. Folding it into
`approval-service` later only means moving the four `domain/dto/repository/service`
classes and the `/api/backups` gateway route.

| Item | Value |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Base package | `com.questify.backup` |
| Port | 8087 |
| Database | MySQL `backup_service` |
| Discovery | Eureka client |

## Build & run

```bash
mvn clean install
mvn spring-boot:run
```

```bash
mvn clean test
```

## Entity

`BackupSet`: `id`, `paperIds` (collection table `backup_set_papers`), `label`,
`createdBy`, `institutionId`, `createdAt`, `storageReference`.

## API (role: Reviewer)

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/backups` | Backup sets for the caller's institution, newest first |
| GET | `/api/backups/{id}` | Single backup set (cross-institution access is `403`) |
| POST | `/api/backups` | Creates a set from `{ label, paperIds, storageReference? }` |

JWT validation is identical to the other QuestiFy services
(`Authorization: Bearer <jwt>`, shared `questify.jwt.secret`, roles from the
`roles` claim).

Frontend route: `/backups`.

## API Gateway routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: backup-service
          uri: lb://BACKUP-SERVICE
          predicates:
            - Path=/api/backups/**
```
