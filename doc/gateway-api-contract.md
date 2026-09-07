# Gateway API Contract

## Overview

- Repository: `ti-gateway-api`
- Microservice: `gateway`

Notes:
- `GatewayExtController` (`src/main/java/com/wk/ti/controller/GatewayExtController.java`) declares a forwarding endpoint, but its `@RestController` and `@RequestMapping("/rest/v1/**")` annotations are commented out in source, so the class is not an active Spring bean/controller. It is excluded from this contract.
- `RestExceptionHandler` is annotated `@RestControllerAdvice` (not `@RestController`) and only contains `@ExceptionHandler` methods, not request-mapped endpoints, so it is excluded from this contract.
- `GatewayController` acts as a generic reverse-proxy: most of its endpoints forward the incoming request to a downstream service resolved at runtime (`DownstreamProtectUrlService`) and return whatever the downstream service returned (via `RestTemplateService.exchange`, which itself may return a parsed JSON body, a raw string, binary bytes, or an error DTO depending on the downstream response). For these endpoints the actual response body/status is genuinely dynamic and cannot be determined from this repository's source code alone.

## VersionController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Version->Get Application Version (unauthenticated) | GET `<server address>/rest/v1/version` | `"1.0.0"` (plain text response, `Content-Type: text/plain`) |

## GatewayController

Class-level mapping: `@RequestMapping({"/api/v1"})`

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Gateway->Subscribe to AI Assistant SSE Stream (authenticated) | GET `<server address>/api/v1/ai-orchestrator/sse/subscription/{conversationId}/{questionId}` <br> Path variables: `conversationId: string` (e.g. `conv-12345`), `questionId: number` (Long, e.g. `98765`) | Streaming response (`Content-Type: text/event-stream`), not a JSON body. Frames are proxied/re-emitted from the downstream SSE stream, e.g.:<br>`event: data`<br>`data: {"statusCodeValue":200,...}`<br><br>`event: ping`<br>`data: heartbeat` |
| Gateway->Trigger AI Assistant Question (SSE Passthrough) (authenticated) | GET `<server address>/api/v1/ai-orchestrator/sse/question?conversationId=conv-12345&questionId=98765` <br> Query params: `conversationId: string` (required, no default), `questionId: number` (Long, required, no default) | returns ResponseEntity (body/status forwarded from downstream; cannot be determined from source code) |
| Gateway->Upload File (Proxy) (authenticated) | POST `<server address>/api/v1/<microservice-name>/**/upload` <br> (wildcard path segment; multipart request) | Req: `multipart/form-data` with field `file: MultipartFile` (binary, required) · Resp: HTTP 200 OK; response body wraps the downstream `ResponseEntity` object returned by the proxy upload call — actual downstream payload cannot be determined from source code (generic proxy) |
| Gateway->Forward DELETE Request (Proxy Catch-All) (authenticated) | DELETE `<server address>/api/v1/<microservice-name>/**` | Req: raw request body (optional, forwarded to downstream as-is, `@RequestBody(required = false) String`) · Resp: HTTP status and body are mirrored from the downstream service response; cannot be determined from source code (generic passthrough proxy) |
| Gateway->Forward GET Request (Proxy Catch-All) (authenticated) | GET `<server address>/api/v1/<microservice-name>/**` | returns ResponseEntity (body/status forwarded from downstream; cannot be determined from source code) |
| Gateway->Forward POST Request (Proxy Catch-All) (authenticated) | POST `<server address>/api/v1/<microservice-name>/**` | Req: raw request body (optional, forwarded to downstream as-is, `@RequestBody(required = false) String`) · Resp: HTTP status and body are mirrored from the downstream service response; cannot be determined from source code (generic passthrough proxy) |
| Gateway->Forward PUT Request (Proxy Catch-All) (authenticated) | PUT `<server address>/api/v1/<microservice-name>/**` | Req: raw request body (optional, forwarded to downstream as-is, `@RequestBody(required = false) String`) · Resp: HTTP status and body are mirrored from the downstream service response; cannot be determined from source code (generic passthrough proxy) |

## UserController

No class-level `@RequestMapping` (endpoints define full paths individually).

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| User->Return Empty Favicon | GET `<server address>/favicon.ico` <br> (route as literally declared in source — does not follow the `/api/v1` or `/rest/v1` convention, so no microservice-name insertion or auth tag is applied) | `void` method with empty body (no content is written; used only to avoid 404 logging for browser favicon requests) |
| User->Get Current ID Token (authenticated) | GET `<server address>/api/v1/gateway/token` | `{"token": "<idToken value as string>"}` |
| User->Get Current User Details (authenticated) | GET `<server address>/api/v1/gateway/user?redirectId=knowledge-url` <br> Query params: `redirectId: string` (default: `knowledge-url`) | `{"email": "user@example.com", "given_name": "John", "family_name": "Doe", "username": "jdoe", "roles": ["ROLE_USER"], "registered": true}` |
