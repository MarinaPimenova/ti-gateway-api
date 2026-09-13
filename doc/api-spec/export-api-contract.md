# Export API Contract

## Overview

- Repository: `ti-export-api`
- Microservice: `export`

Notes on paths: both controllers in this repository use a `/rest/v1/...` base path (not `/api/v1/...`). Per the documentation convention, endpoints under `/rest/v1` are treated as **unauthenticated**, and the microservice-name insertion rule (which is defined only for `/api/v1/...` mappings) does not apply — the routes below are preserved exactly as declared in source.

## VersionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Version->Get Application Version (unauthenticated) | GET `<server address>/rest/v1/version` | Response (`text/plain`, HTTP 200 default): `MyApp : 1.0.0` |

## ExportController

Class-level mapping: `@RequestMapping("/rest/v1/export")`

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Export->Export Questions Report (unauthenticated) | GET `<server address>/rest/v1/export/{reportType}?fields=all&projects=1&difficulties=1&tags=1`<br>Path variable: `reportType: string`<br>Query params: `fields: string` (default `all`), `projects: List<Long>` (optional), `difficulties: List<Long>` (optional, maps to `difficultiesLevels`), `tags: List<Long>` (optional) | Response: HTTP 201 Created. Body is a binary file stream (`ResponseEntity<ByteArrayResource>`, `produces = application/octet-stream`) containing the generated report (CSV or XLSX depending on `reportType`). Response headers include `Content-Type: application/force-download` and `Content-Disposition: attachment; filename=questions.<csv|xlsx>`. Exact binary content cannot be determined from source code (built dynamically from question data by the resolved `ExportService` implementation). |
