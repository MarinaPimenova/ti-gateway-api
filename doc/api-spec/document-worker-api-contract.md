# Document Worker API Contract

## Overview

- Repository: `ti-document-worker`
- Microservice: `document-worker`

## VersionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Version->Get Application Version (unauthenticated) | GET `<server address>/rest/v1/version` | `"1.0.0"` (plain text response, `Content-Type: text/plain`, not JSON) |

## UploadController

Base mapping: `/rest/v1`

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Upload->Upload Document File (unauthenticated) | POST `<server address>/rest/v1/documents` | Req: multipart/form-data, `file: MultipartFile` · Resp: `{"documentId": 1, "filename": "report.pdf", "status": "UPLOADED"}` (HTTP 200) |
| Upload->Load Document From URL (unauthenticated) | POST `<server address>/rest/v1/load-url` | Req: `{"url": "https://example.com/doc.pdf"}` · Resp: `{"documentId": 1, "filename": "doc.pdf", "status": "UPLOADED"}` (HTTP 200) |

### Notes

- Both controller base paths start with `/rest/v1/...`, so per the documentation convention they are treated as unauthenticated. The `/api/v1` → `/api/v1/document-worker` microservice-name insertion rule applies only to `/api/v1`-prefixed routes; since no controller in this repository uses `/api/v1`, no path insertion was applicable and routes are documented exactly as written in source.
- `FileProcessingResponse` fields (from `com.wk.ti.upload.model.FileProcessingResponse`, a Java record): `documentId: Long`, `filename: String`, `status: DocumentStatus` (enum: `UPLOADED`, `PROCESSING`, `READY`, `FAILED`).
- `loadUrl` accepts `Map<String, String>` and reads the `url` key from it; no other keys are used by the controller.
- `com.wk.ti.controller.RestExceptionHandler` is a `@RestControllerAdvice` (global exception handler), not a `@RestController`, and defines no REST endpoints/routes — it is intentionally excluded from this contract.
