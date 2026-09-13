# Orchestrator API Contract

## Overview

- Repository: `ti-orchestrator-api`
- Microservice: `orchestrator`

> Note: This repository contains two `@RestController` classes. `VersionController` maps its endpoint under `/rest/v1/...` (unauthenticated per convention); the microservice name `orchestrator` is inserted immediately after the first path segment (`/rest/v1`), mirroring the `/api/v1` insertion rule. `ImportController` maps its endpoints under `/api/v1/import` (authenticated per convention).

## VersionController

Source: `src/main/java/com/wk/ti/controller/VersionController.java` — no class-level `@RequestMapping`; mapping is declared directly on the method.

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Version->Get Application Version (unauthenticated) | GET `<server address>/rest/v1/orchestrator/version` | `"ti-orchestrator-api : <project.version>"` — plain text response (`produces = MediaType.TEXT_PLAIN_VALUE`), built as `name + " : " + version` from `info.app.name` / `info.app.version` (`VersionService.getVersion()`). HTTP 200 OK (default, no explicit status manipulation in source). |

## ImportController

Class-level mapping: `@RequestMapping("/api/v1/import")`

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Import->Subscribe to Import Status (authenticated) | GET `<server address>/api/v1/orchestrator/import/subscribe/{importId}` (example: `/api/v1/orchestrator/import/subscribe/3fa85f64-5717-4562-b3fc-2c963f66afa6`) | Path variable `importId: string`. Returns a raw `SseEmitter` (`produces = MediaType.TEXT_EVENT_STREAM_VALUE`), not `ResponseEntity`, so no fixed JSON body/status code is set in the controller. The stream is fed asynchronously by `SseEmitterRegistry`/`ImportResultListener`, which emits a single named SSE event `import-result` once processing finishes, with data being either an `ImportCompletedEvent` (`{"importId": "3fa85f64-5717-4562-b3fc-2c963f66afa6", "processedRows": 120}`) on success or an `ImportFailedEvent` (`{"importId": "3fa85f64-5717-4562-b3fc-2c963f66afa6", "reason": "Invalid file format"}`) on failure, then completes the emitter (or times out after 180 seconds / completes with error). Exact wire-level HTTP status of the initial response cannot be determined beyond the default 200 OK for an opened SSE stream. |
| Import->Upload File (authenticated) | POST `<server address>/api/v1/orchestrator/import` | Req: multipart/form-data; part `file`: MultipartFile (binary) · Resp: `{"importId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"}` — HTTP 200 OK (`ResponseEntity.ok(importService.upload(file))`). |
