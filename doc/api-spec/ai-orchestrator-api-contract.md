# AI Orchestrator API Contract

## Overview

- Repository: `ti-ai-orchestrator-api`
- Microservice: `ai-orchestrator`

> Note: All `@RestController` classes in this repository map their endpoints under `/rest/v1/...` (none use `/api/v1/...`). Per the authenticated/unauthenticated convention, every endpoint below is therefore **unauthenticated**. The microservice name `ai-orchestrator` is inserted immediately after the first path segment (`/rest/v1`), mirroring the `/api/v1` insertion rule.

## VersionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Version->Get Service Version (unauthenticated) | GET `<server address>/rest/v1/ai-orchestrator/version` | Response (`text/plain`, HTTP 200): `"ai-orchestrator : 1.0.0"` |

## QuestionController

Class-level mapping: `/rest/v1`

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Questions->Ask Question (unauthenticated) | POST `<server address>/rest/v1/ai-orchestrator/question?conversationId=abc-123` <br> Query params: `conversationId: string` | Req: `{ "question": "string" }` · Resp (HTTP 200): `{ "conversationId": "abc-123", "questionId": "string", "question": "string" }` |
| Questions->Submit Feedback | POST `<server address>/rest/v1/ai-orchestrator/feedback?conversationId=abc-123` <br> Query params: `conversationId: string` | Req: `{ "questionId": 123, "feedback": "string" }` · Resp: HTTP 200 (empty body, `ResponseEntity<Void>` via `ResponseEntity.ok().build()`) |

Note: a `GET /chats/{chatId}/questions` handler exists in this controller's source but is entirely commented out (`/* ... */`) and therefore not an active endpoint; it was excluded per the "only document what exists/executes" rule.

## SseController

Class-level mapping: `/rest/v1`

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| SSE->Subscribe to Question Stream (unauthenticated) | GET `<server address>/rest/v1/ai-orchestrator/sse/subscription/{conversationId}/{questionId}` <br> Path params: `conversationId: string`, `questionId: number` <br> Example: `/rest/v1/ai-orchestrator/sse/subscription/abc-123/456` | Cannot be determined from source code — method returns `SseEmitter` (`text/event-stream`), a streaming connection whose emitted event payloads are produced asynchronously by `SseService` and are not a single fixed JSON body. |
| SSE->Trigger Answer Processing | GET `<server address>/rest/v1/ai-orchestrator/sse/question?conversationId=abc-123&questionId=456` <br> Query params: `conversationId: string`, `questionId: number` | Resp: HTTP 200 (empty body, `ResponseEntity<Void>` via `ResponseEntity.ok().build()`) |
| SSE->Cancel Subscription | DELETE `<server address>/rest/v1/ai-orchestrator/sse/subscription/{conversationId}/{questionId}` <br> Path params: `conversationId: string`, `questionId: number` <br> Example: `/rest/v1/ai-orchestrator/sse/subscription/abc-123/456` | Req: none · Resp: HTTP 200 (empty body, `ResponseEntity<Void>` via `ResponseEntity.ok().build()` — not 204, per actual code) |
| SSE->Cancel Multiple Subscriptions | POST `<server address>/rest/v1/ai-orchestrator/sse/subscriptions/cancel` | Req: `{ "subscriptions": [ { "conversationId": "abc-123", "questionId": 456 } ] }` · Resp: HTTP 200 (empty body, `ResponseEntity<Void>` via `ResponseEntity.ok().build()`) |

## ChatController

Class-level mapping: `/rest/v1`

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Chats->Create Chat and Store Question (unauthenticated) | POST `<server address>/rest/v1/ai-orchestrator/lp/chat?conversationId=abc-123` <br> Query params: `conversationId: string` | Req: `{ "question": "string" }` · Resp (HTTP 200, `text/plain` body): `"Ok"` |
| Chats->Rename Chat | POST `<server address>/rest/v1/ai-orchestrator/chat/name?conversationId=abc-123` <br> Query params: `conversationId: string` | Req: `{ "newName": "string" }` · Resp: HTTP 200 (empty body — method declares `ResponseEntity<String>` but returns `ResponseEntity.ok().build()`) |
| Chats->Delete Chat | DELETE `<server address>/rest/v1/ai-orchestrator/chat?conversationId=abc-123` <br> Query params: `conversationId: string` | Req: none · Resp: HTTP 200 (empty body — method declares `ResponseEntity<String>` but returns `ResponseEntity.ok().build()`) |

Note: a `GET /chats` handler exists in this controller's source but is entirely commented out (`/* ... */`) and therefore not an active endpoint; it was excluded per the "only document what exists/executes" rule.
