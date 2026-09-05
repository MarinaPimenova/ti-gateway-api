# Local Development Environment

## Overview

The **TI Knowledge Platform** is a set of Spring Boot microservices, background workers,
AI agents and React UIs, fronted by `ti-gateway-api`. `docker/docker-compose-full.yml`
runs the entire platform in Docker, including logging/tracing/metrics — the closest
local equivalent to a production deployment.

| Component            | Repository               | Image                        | Port (host) |
|-----------------------|--------------------------|-------------------------------|--------------|
| Gateway               | `ti-gateway-api`         | `ti-gateway-local`            | 8080         |
| Knowledge API         | `ti-knowledge-api`       | `ti-knowledge-local`          | 8081         |
| Orchestrator API      | `ti-orchestrator-api`    | `ti-orchestrator-local`       | 8082         |
| Import Worker         | `ti-import-worker`       | `ti-import-worker-local`      | 8083         |
| Export API            | `ti-export-api`          | `ti-export-local`             | 8084         |
| AI Orchestrator API   | `ti-ai-orchestrator-api` | `ti-ai-orchestrator-local`  | 8085         |
| Document Worker       | `ti-document-worker`     | `ti-document-worker-local`    | 8086         |
| Document Agent        | `ti-document-agent`      | `ti-document-agent-local`     | 8087         |
| SQL Agent             | `ti-sql-agent`           | `ti-sql-agent-local`          | 8088         |
| Knowledge UI          | `ti-knowledge-ui`        | `ti-knowledge-ui-local`       | 5000         |
| AI Chatbot UI         | `ti-ai-chatbot-ui`       | `ti-ai-chatbot-local`         | 7000         |
| AI Question UI        | `ti-ai-question-ui`      | `ti-ai-question-local`        | 4000         |

Infrastructure started alongside the services: PostgreSQL (`ti-knowledge-db`,
`ti-document-db` with pgvector, `ti-assistant-db`), Redis (session cache + memory-prompt
cache), RabbitMQ, and an observability stack (Prometheus, Loki, Zipkin, Grafana).

---

## Prerequisites

| Tool           | Version |
|----------------|---------|
| Docker Desktop | Latest  |
| Docker Compose | v2+     |
| Java           | 21      |
| Maven          | 3.9+ (gateway) |
| Gradle wrapper | bundled with each service repo |
| Node.js        | 22+ (UIs) |
| Git            | Latest  |

```bash
docker --version
docker compose version
java -version
node --version
```

You will also need the sibling repositories checked out next to `ti-gateway-api`
(one directory per component listed above) — each has its own Dockerfile.

### Okta developer account

1. Sign up at https://developer.okta.com/signup/.
2. Create an Okta Application in **Web** mode ([setup guide](https://developer.okta.com/docs/guides/implement-grant-type/authcode/main/#1-setting-up-your-application)). Use the wizard defaults.
3. Under **General Settings**, add to **Login redirect URIs**: `http://localhost:8080/authorization-code/callback`.
4. Under **General Settings**, add to **Logout redirect URIs**: `http://localhost:8080`.
5. Under **Assignments**, assign the app to "Everyone" or the group/users that need access.
6. Note the Okta domain, client ID and client secret — you'll need them below.

---

## 1. Configure environment variables

```bash
cd docker
cp env.example env
```

Edit `env` and fill in, at minimum:

- `OKTA_DOMAIN`, `OKTA_OAUTH2_CLIENT_ID`, `OKTA_OAUTH2_CLIENT_SECRET`
- `ADMINS`
- `OPEN_AI_API_KEY`, `OPEN_AI_ENDPOINT`, `OPEN_AI_COMPLETIONS_PATH`, `CHAT_MODEL`
- `MISTRAL_AI_API_KEY` (only if document ingestion uses Mistral)

Every other variable in `env.example` already has a working default inside
`docker-compose-full.yml` for the all-in-Docker network (database/queue hostnames,
storage paths, pgvector settings, etc.) — only override those if you need
non-default behavior. Never commit the filled-in `env` file.

---

## 2. Build the service images

Each service repository builds its own artifact and Docker image, tagged exactly
as referenced in `docker-compose-full.yml`:

```bash
# Spring Boot services (Maven example: ti-gateway-api)
cd ../ti-gateway-api
mvn clean package -DskipTests
docker build -t ti-gateway-local:latest .

# Spring Boot services built with Gradle (ti-knowledge-api, ti-orchestrator-api,
# ti-import-worker, ti-export-api, ti-ai-orchestrator-api, ti-document-worker,
# ti-document-agent, ti-sql-agent)
cd ../ti-knowledge-api
./gradlew clean build -x test
docker build -t ti-knowledge-local:latest .
# ... repeat per service, using the image tag from the table above

# React UIs (ti-knowledge-ui, ti-chatbot-ui)
cd ../ti-knowledge-ui
npm install
npm run build
docker build -t ti-knowledge-ui-local:latest .
```

Repeat the Gradle/npm pattern for the remaining services, using the image tag
from the table in the Overview section. `docker compose` will refuse to start a
service whose image hasn't been built yet.

---

## 3. Start the platform

```bash
cd docker
docker compose -f docker-compose-full.yml --env-file env up -d
```

Check status:

```bash
docker compose -f docker-compose-full.yml ps
```

Stop everything:

```bash
docker compose -f docker-compose-full.yml down
```

Stop and wipe all data (databases, RabbitMQ, storage volumes):

```bash
docker compose -f docker-compose-full.yml down -v
```

Once every container is up and healthy, open **http://localhost:8080** in your
browser — the gateway handles the Okta login and redirects you into the
Knowledge UI dashboard.

---

## Access URLs

| What                 | URL                                      |
|----------------------|-------------------------------------------|
| Gateway               | http://localhost:8080                    |
| Knowledge UI          | http://localhost:5000                    |
| AI Chatbot UI         | http://localhost:7000                    |
| AI Question UI        | http://localhost:4000                    |
| RabbitMQ management   | http://localhost:15672 (`admin` / `admin`, from `docker/rabbitmq/definitions.json`) |
| Grafana               | http://localhost:3000 (`admin` / value of `GRAFANA_ADMIN_PASS`) |
| Prometheus            | http://localhost:9090                    |
| Zipkin                | http://localhost:9411                    |

Gateway health check: `http://localhost:8080/actuator/health` — likewise
`/actuator/health` on each backend service's own port (8081-8088).

---

## Logs & debugging

```bash
# all services
docker compose -f docker-compose-full.yml logs -f

# one service
docker compose -f docker-compose-full.yml logs -f ti-gateway-api

# shell into a container
docker exec -it ti-knowledge-api sh

# inspect a database
docker exec -it ti-knowledge-db psql -U knowledge_user -d knowledge_db
```

Structured logs are also shipped to Loki (`x-logging` driver in the compose file)
and viewable in Grafana; traces go to Zipkin via `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT`.

---

## Common issues

**Port already in use**
```bash
lsof -i :8080
```
Stop the conflicting process or change the host-side port mapping in
`docker-compose-full.yml`.

**Service can't reach a database/queue** — inside the `knowledge-network`,
containers must address each other by container name and the *container's
internal* port (e.g. `ti-document-db:5432`, not the host-mapped `5433`).
`docker compose -f docker-compose-full.yml config` will render the fully
resolved configuration if you want to double check a value.

**Okta login/redirect fails** — confirm the redirect and logout URIs configured
in the Okta app match `APPLICATION_URL` exactly, and that `OKTA_DOMAIN` has no
`https://` prefix.

**Image not found** — the image for a service hasn't been built yet; see
[Build the service images](#2-build-the-service-images).

---

## Alternative: infrastructure-only mode

To run the Spring Boot services from your IDE against dockerized infrastructure
only (faster edit/debug loop), use `docker-compose-infra.yml` instead — see
`docker/README.md` for details.
