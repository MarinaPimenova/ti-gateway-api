# Knowledge API Contract

## Overview

- Repository: `ti-knowledge-api`
- Microservice: `knowledge`

Note: `RestExceptionHandler` (`src/main/java/com/wk/ti/controller/RestExceptionHandler.java`) is annotated with `@RestControllerAdvice`, not `@RestController`, and exposes no HTTP mapping endpoints — it is a global exception handler and is excluded from this contract.

## VersionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Version->Get Application Version (unauthenticated) | GET `<server address>/rest/v1/knowledge/version` | `1.0.0` (plain text response, `Content-Type: text/plain`) |

## QuestionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Questions->List All Questions (authenticated) | GET `<server address>/api/v1/knowledge/questions` | `[{"id": 1, "tags": "Java, Spring", "question": "What is dependency injection?", "shortAnswer": "A design pattern...", "resources": "https://example.com/di", "projectName": "Core Training"}]` |
| Questions->Search by Pattern | GET `<server address>/api/v1/knowledge/questions/search?pattern=spring` <br> Query params: `pattern: string` (default: ``) | `[{"id": 1, "tags": "Java, Spring", "question": "What is dependency injection?", "shortAnswer": "A design pattern...", "resources": "https://example.com/di", "projectName": "Core Training"}]` |
| Questions->List Recently Added Questions | GET `<server address>/api/v1/knowledge/questions/recent?limit=3` <br> Query params: `limit: number` (int, default: `3`) | `[{"id": 1, "question": "What is dependency injection?", "tag": "Spring", "level": "A2", "updatedAt": "2026-09-01T10:15:30Z", "snippetPreview": "A design pattern used to..."}]` |
| Questions->Count Questions | GET `<server address>/api/v1/knowledge/questions/count` | `42` |
| Questions->Count Questions by Tag | GET `<server address>/api/v1/knowledge/questions/tags/count` | `[{"tag": "Spring", "count": 12}]` |
| Questions->Get Question by ID | GET `<server address>/api/v1/knowledge/questions/{id}` <br> Path variables: `id: number` (Long, e.g. `1`) | `{"id": 1, "question": "What is dependency injection?", "shortAnswer": "A design pattern...", "detailedAnswer": "Dependency injection is...", "questionLevel": {"questionLevelId": 2, "difficultyCode": "A2"}, "codeExample": {"language": "java", "sourceCode": "class Foo {}"}, "tags": [{"id": 3, "tag": "Spring"}], "resources": [{"id": 5, "url": "https://example.com/di", "description": "DI overview"}], "projects": [{"id": 7, "name": "Core Training"}], "createdBy": "jdoe", "updatedBy": "jdoe", "createdDate": "2026-08-01T09:00:00Z", "updatedDate": "2026-09-01T10:15:30Z"}` |
| Questions->Create Question (authenticated) | POST `<server address>/api/v1/knowledge/questions` | Req: `{"question": "What is dependency injection?", "shortAnswer": "A design pattern...", "detailedAnswer": "Dependency injection is...", "questionLevelId": 2, "codeExample": {"language": "java", "sourceCode": "class Foo {}"}, "tagIds": [3], "projectIds": [7], "resources": [{"url": "https://example.com/di", "description": "DI overview"}]}` · Resp: HTTP 201 Created — `{"id": 1, "question": "What is dependency injection?", "shortAnswer": "A design pattern...", "detailedAnswer": "Dependency injection is...", "questionLevel": {"questionLevelId": 2, "difficultyCode": "A2"}, "codeExample": {"language": "java", "sourceCode": "class Foo {}"}, "tags": [{"id": 3, "tag": "Spring"}], "resources": [{"id": 5, "url": "https://example.com/di", "description": "DI overview"}], "projects": [{"id": 7, "name": "Core Training"}], "createdBy": "jdoe", "updatedBy": "jdoe", "createdDate": "2026-08-01T09:00:00Z", "updatedDate": "2026-09-01T10:15:30Z"}` |
| Questions->Update Question | PUT `<server address>/api/v1/knowledge/questions/{id}` <br> Path variables: `id: number` (Long, e.g. `1`) | Req: `{"question": "What is dependency injection?", "shortAnswer": "A design pattern...", "detailedAnswer": "Dependency injection is...", "questionLevel": {"questionLevelId": 2, "difficultyCode": "A2"}, "codeExample": {"language": "java", "sourceCode": "class Foo {}"}, "tags": [{"id": 3, "tag": "Spring"}], "resources": [{"id": 5, "url": "https://example.com/di", "description": "DI overview"}], "projects": [{"id": 7, "name": "Core Training"}], "createdBy": "jdoe", "updatedBy": "jdoe", "createdDate": "2026-08-01T09:00:00Z", "updatedDate": "2026-09-01T10:15:30Z"}` · Resp: HTTP 200 OK — `{"id": 1, "question": "What is dependency injection?", "shortAnswer": "A design pattern...", "detailedAnswer": "Dependency injection is...", "questionLevel": {"questionLevelId": 2, "difficultyCode": "A2"}, "codeExample": {"language": "java", "sourceCode": "class Foo {}"}, "tags": [{"id": 3, "tag": "Spring"}], "resources": [{"id": 5, "url": "https://example.com/di", "description": "DI overview"}], "projects": [{"id": 7, "name": "Core Training"}], "createdBy": "jdoe", "updatedBy": "jdoe", "createdDate": "2026-08-01T09:00:00Z", "updatedDate": "2026-09-01T10:15:30Z"}` |
| Questions->Delete Question | DELETE `<server address>/api/v1/knowledge/questions/{id}` <br> Path variables: `id: number` (Long, e.g. `1`) | Req: none · Resp: HTTP 204 No Content (empty body) |

## PublicQuestionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Questions->List Recently Added Questions (unauthenticated) | GET `<server address>/rest/v1/knowledge/questions/recent?limit=3` <br> Query params: `limit: number` (int, default: `3`) | `[{"id": 1, "question": "What is dependency injection?", "tag": "Spring", "level": "A2", "updatedAt": "2026-09-01T10:15:30Z", "snippetPreview": "A design pattern used to..."}]` |
| Questions->Count Questions (unauthenticated) | GET `<server address>/rest/v1/knowledge/questions/count` | `42` |
| Questions->Count Questions by Tag (unauthenticated) | GET `<server address>/rest/v1/knowledge/questions/tags/count` | `[{"tag": "Spring", "count": 12}]` |

## TagController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Tags->List All Tags (authenticated) | GET `<server address>/api/v1/knowledge/tags` | `[{"id": 3, "tag": "Spring"}]` |

## ProjectController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Projects->List All Projects (authenticated) | GET `<server address>/api/v1/knowledge/projects` | `[{"id": 7, "name": "Core Training"}]` |
| Projects->Count Projects | GET `<server address>/api/v1/knowledge/projects/count` | `5` |

## PublicProjectController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Projects->Count Projects (unauthenticated) | GET `<server address>/rest/v1/knowledge/projects/count` | `5` |

## QuestionLevelController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| QuestionLevels->List All Question Levels (authenticated) | GET `<server address>/api/v1/knowledge/qlevels` | `[{"questionLevelId": 2, "difficultyCode": "A2"}]` |
