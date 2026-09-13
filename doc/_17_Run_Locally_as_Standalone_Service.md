
# Run Locally as a Standalone Service

This section explains how to run `ti-gateway-api` on your host machine while its dependencies
(databases, RabbitMQ, backend microservices) run in Docker, and how to exercise the platform's
functionality using the `.http` files under [`http/`](http).

## 1. Choose a Docker Compose profile

All compose files live under [`docker/`](docker). Copy `docker/env.example` to `docker/env` and
fill in the **required** values (`OKTA_*`, `ADMINS`, `OPEN_AI_*`) before starting anything.

| Profile | Command (from `docker/`) | What runs in Docker | What you run locally |
|---|---|---|---|
| **Infra only** | `docker compose -f docker-compose-infra.yml --env-file env up -d` | Redis, RabbitMQ, `ti-knowledge-db`, `ti-document-db`, `ti-assistant-db`, Prometheus, Loki, Zipkin, Grafana | `ti-gateway-api` + any backend service(s) you're iterating on, started from IntelliJ IDEA |
| **Full stack** | `./run-compose.sh` (wraps `docker compose -f docker-compose-full.yml --env-file env up`) | Every service in the diagram above — UIs, gateway, all APIs/workers/agents, and infra | Nothing — pure black-box functional testing |
| **Individual infra pieces** | e.g. `docker compose -f _04_knowledge_postgres.yaml up` | One service at a time | Everything else |

`run-compose.sh` requires the local images to be built first (`ti-gateway-local`,
`ti-knowledge-local`, `ti-ui-local`, etc. — see `docker/README.md` and `k8s/build-all-target-and-image.sh`).

## 2. Verify the gateway is up

```text
GET http://localhost:8080/rest/v1/version
Accept: text/plain
```
```text
GET http://localhost:8080/actuator/health
Accept: application/json
```
See [`http/gateway.http`](http/gateway.http) and [`http/version.http`](http/version.http).

## 3. Authenticate (required for any `/api/v1/**` call)

`/api/**` requires an authenticated session (`SecurityConfig`); the gateway does **not** accept a
client-supplied JWT — it injects its own OIDC id token when forwarding to backends. To test
proxied endpoints:

1. Open `http://localhost:8080` in a browser and log in through Okta.
2. Open dev tools → Application/Storage → Cookies, copy the `SESSION` cookie value.
3. Paste it into the `Cookie: SESSION=<SESSION_COOKIE_VALUE>` header in the `.http` files below.

## 4. Known gateway routing limitation

`GatewayController` proxies only paths under `/api/v1/**`, and forwards them downstream **with
the `/api/v1` prefix preserved**, stripping just the `/{service}` segment
(`DownstreamService.getDownstreamServiceUrl()`). This means a backend is only reachable through
the gateway if its own controllers are mapped under `/api/v1/**`.
`GatewayExtController` proxies only paths under `/rest/v1/**`, and forwards them downstream **with
the `/rest/v1` prefix preserved**, stripping just the `/{service}` segment
(`DownstreamService.getDownstreamServiceUrl()`).

Today that's true only for
`ti-knowledge-api` and the import feature of `ti-orchestrator-api`; every other service currently
exposes its business endpoints under `/rest/v1/**`, which the gateway does not forward — those
must be called **directly** against the service's own port for standalone testing.

## 5. `.http` files under [`http/`](http)

| File | Target | Via gateway? | Base URL |
|---|---|---|---|
| `gateway.http` | ti-gateway-api (version/health/token/logout) | n/a (native) | `localhost:8080` |
| `user.http` | ti-gateway-api (current user) | n/a (native) | `localhost:8080` |
| `okta.http` | Okta OIDC discovery | n/a | Okta domain |
| `swagger.http` | Aggregated OpenAPI UI | n/a (native) | `localhost:8080` |
| `knowledge.http` | ti-knowledge-api | ✅ `/api/v1/knowledge/**` | `localhost:8080` |
| `orchestrator.http` | ti-orchestrator-api (import) | ✅ `/api/v1/orchestrator/**` | `localhost:8080` |
| `import-worker.http` | ti-import-worker | ❌ direct | `localhost:8083` |
| `export-api.http` | ti-export-api | ❌ direct | `localhost:8084` |
| `ai-orchestrator.http` | ti-ai-orchestrator-api | ❌ direct | `localhost:8085` |
| `document-worker.http` | ti-document-worker | ❌ direct | `localhost:8086` |
| `document-agent.http` | ti-document-agent | ❌ direct | `localhost:8087` |

`ti-sql-agent` is intentionally excluded (out of scope / no stable REST surface at this time).

## 6. API Reference

Full runnable requests live in the `.http` files above; below is the endpoint inventory for every
BE microservice reachable from this platform (`ms.service-name-to-uri` keys, excluding `sql-agent`).

### ti-knowledge-api — via gateway `/api/v1/knowledge/**` (port 8081)

```text
GET http://localhost:8080/api/v1/knowledge/questions
Accept: application/json
Cookie: SESSION=<SESSION_COOKIE_VALUE>
```
Response: `[{ "id": 101, "tag": "spring-boot", "question": "...", "shortAnswer": "...", "resourceUrl": "...", "projectName": "..." }]`

```text
GET http://localhost:8080/api/v1/knowledge/questions/1
```
Response: `{ "id": 101, "question": "...", "shortAnswer": "...", "detailedAnswer": "...", "questionLevel": {...}, "tags": [...], "resources": [...], "projects": [...] }`

```text
POST http://localhost:8080/api/v1/knowledge/questions
Content-Type: application/json
Accept: application/json
Cookie: SESSION=<SESSION_COOKIE_VALUE>

{
  "question": "What is durability?",
  "shortAnswer": "Durability ensures that changes applied by a committed transaction survive system failures.",
  "questionLevelId": 1,
  "tagIds": [17],
  "projectIds": [],
  "resources": []
}
```
Response (`201`): `QuestionDetails` with assigned `id`.

```text
PUT http://localhost:8080/api/v1/knowledge/questions/1
DELETE http://localhost:8080/api/v1/knowledge/questions/1
GET http://localhost:8080/api/v1/knowledge/projects
GET http://localhost:8080/api/v1/knowledge/tags
GET http://localhost:8080/api/v1/knowledge/qlevels
```

### ti-orchestrator-api — via gateway `/api/v1/orchestrator/**` (port 8082)

```text
POST http://localhost:8080/api/v1/orchestrator/import
Content-Type: multipart/form-data
Cookie: SESSION=<SESSION_COOKIE_VALUE>

file=questions.csv  (columns: QUESTION, LEVEL, SHORT ANSWER, RESOURCE)
```
Response: `{ "importId": "3f2504e0-4f89-11d3-9a0c-0305e82c3301" }`

```text
GET http://localhost:8080/api/v1/orchestrator/import/subscribe/{importId}
Accept: text/event-stream
```
Response: SSE stream of import progress/completion events.

### ti-import-worker — direct (port 8083, not proxied)

```text
POST http://localhost:8083/rest/v1/import
Content-Type: multipart/form-data

file=questions.csv
```
Response: `{ "importId": "3f2504e0-4f89-11d3-9a0c-0305e82c3301" }`

### ti-export-api — direct (port 8084, not proxied)

```text
GET http://localhost:8084/rest/v1/export/{reportType}?fields=all&projects=1&difficulties=2&tags=17
Accept: application/octet-stream
```
`reportType`: `csv` | `excel`. Response: binary file attachment (`questions.csv` / `questions.xlsx`).

### ti-ai-orchestrator-api — direct (port 8085, not proxied)

```text
POST http://localhost:8085/rest/v1/question?conversationId={conversationId}
Content-Type: application/json
Accept: application/json

{ "question": "What was our total revenue in Q2 2026?" }
```
Response: `{ "conversationId": "...", "questionId": "10432", "question": "..." }`

```text
GET http://localhost:8085/rest/v1/sse/subscription/{conversationId}/{questionId}
Accept: text/event-stream
```
Response: SSE stream of incremental AI answer chunks.

```text
GET http://localhost:8085/rest/v1/sse/question?conversationId={conversationId}&questionId={questionId}
POST http://localhost:8085/rest/v1/feedback?conversationId={conversationId}      { "questionId": 10432, "feedback": "helpful" }
POST http://localhost:8085/rest/v1/chat/name?conversationId={conversationId}     { "newName": "Q2 Sales Analysis" }
DELETE http://localhost:8085/rest/v1/chat?conversationId={conversationId}
```

### ti-document-worker — direct (port 8086, not proxied)

```text
POST http://localhost:8086/rest/v1/documents
Content-Type: multipart/form-data

file=quarterly-report.pdf
```
```text
POST http://localhost:8086/rest/v1/load-url
Content-Type: application/json

{ "url": "https://example.com/docs/annual-compliance-guide.pdf" }
```
Response (both): `{ "documentId": null, "filename": "quarterly-report.pdf", "status": "PROCESSING" }`

### ti-document-agent — direct (port 8087, not proxied)

```text
POST http://localhost:8087/rest/v1/docs?conversationId={conversationId}
Content-Type: application/json
Accept: application/json

{ "questionId": 42, "question": "What are the key risk factors mentioned in the uploaded document?" }
```
Response: `{ "conversationId": "...", "questionId": 42, "termList": "...", "agentType": "DOCUMENT", "sourceSet": {...}, "documentSet": {...}, "summary": "..." }`

```text
GET http://localhost:8087/rest/v1/documents
```
Response: `[{ "id": 101, "filename": "Annual Report 2025.pdf" }]`

```text
POST http://localhost:8087/rest/v1/documents/{id}/question-generation
Content-Type: application/json

{ "userMessage": "Generate questions about the compliance obligations described in this document.", "requestedQuestionCount": 3 }
```
Response: `[{ "question": "...", "answer": "...", "level": {...}, "tags": [...], "resources": [...] }]`

---

Every service above also exposes `GET /rest/v1/version` (plain text) directly on its own port for
a quick health/liveness check.


