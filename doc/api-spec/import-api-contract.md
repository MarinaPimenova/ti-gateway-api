# Import API Contract

## Overview

- Repository: `ti-import-worker`
- Microservice: `import`

## VersionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Version->Get Version (unauthenticated) | GET `<server address>/rest/v1/import/version` | `"1.0.0"` |

## ImportController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Import->Upload File (unauthenticated) | POST `<server address>/rest/v1/import/import` | Req: multipart/form-data; part `file`: MultipartFile (binary) · Resp: `{"importId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"}` |
