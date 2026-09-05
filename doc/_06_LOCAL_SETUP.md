
# Local Development Environment

## Overview

The **TI Knowledge Platform** provides a complete local development environment using **Docker Compose**.

The local environment allows developers to run the complete microservices platform on a developer workstation without requiring external cloud infrastructure.

The local setup includes:

- React + Vite frontend
- Spring Boot 4 microservices
- PostgreSQL databases
- Redis caches
- RabbitMQ message broker
- Okta development integration
- Observability components (optional)

---

# Local Architecture

# Prerequisites

Install the following tools:

| Tool           | Version |
| -------------- | ------- |
| Docker Desktop | Latest  |
| Docker Compose | v2+     |
| Java           | 21      |
| Maven          | 3.9+    |
| Node.js        | 22+     |
| npm            | Latest  |
| Git            | Latest  |

Verify installation:

```bash
docker --version

docker compose version

java -version

mvn -version

node --version

npm --version
```

---

# Docker Compose Overview

## The local environment is started using:

```bash
cd docker
docker compose -f docker-compose-full.yml up -d
```

Docker Compose starts:

* Backend microservices
* Databases
* Redis caches
* RabbitMQ
* Supporting infrastructure

## Prerequisites

Before running this app, you will need the following:

### Init Okta Developer Account

* An Okta Developer Account, you can sign up for one at https://developer.okta.com/signup/.
  <img alt="auth0-okta.png" height="150" src="auth0-okta.png" width="80"/>
* An Okta Application, configured for Web mode. This is done from the Okta Developer Console and you can find instructions [here][https://developer.okta.com/docs/guides/implement-grant-type/authcode/main/#1-setting-up-your-application].  When following the wizard, use the default properties.
* This Okta Application entry needs a login redirect URI. Go to "Login redirect URIs" under "General Settings" for the application, click "Edit" and add http://localhost:8080/authorization-code/callback.
* This Okta Application entry needs the logout callback. "Logout redirect URIs" under "General" for the application should list http://localhost:8080. If it is not present, click "Edit" and add it.
* Ensure that this Okta Application is assigned to "Everyone" group or a custom group or a set of users that need to access the application. Navigate to "Assignments" tab for the application, and click "Assign -> Assign to People" or "Assign -> Assign to Groups" to do this.

### Provide ENVIRONMENT VARIABLES Values



# Starting the Environment

## Clone Repository

```bash
git clone <repository-url>

cd <repository>
```

---

## Build Backend Services

Build all Spring Boot applications:

```bash
mvn clean package
or
./gradlew clean build
```

The build creates:

```text
target/*.jar
or
build/libs/*.jar
```

---

## Build Docker Images

Build all application images:

```bash
docker compose build
```

Example images:

```text
ti-knowledge-ui-local:latest

ti-ai-chatbot-local:latest

ti-ai-question-local:latest

ti-gateway-local:latest

ti-knowledge-local:latest

ti-orchestrator-local:latest

ti-import-worker-local

ti-export-local:latest

ti-ai-orchestrator-local:latest

ti-document-worker-local:latest

ti-document-agent-local:latest

ti-sql-agent-local:latest

```

---

## Start Infrastructure

Start Redis:
```bash
```

Start databases and messaging:

```bash
docker compose up -d postgres rabbitmq
```

Verify:

```bash
docker ps
```

Expected containers:

```text
postgres

rabbitmq
```

---

## Start Complete Platform

Start all services:

```bash
docker compose up
```

or run in background:

```bash
docker compose up -d
```

---

# Service Ports

Default local ports:

| Service             | Port  |
|---------------------| ----- |
| React UI            | 3000  |
| Gateway API         | 8080  |
| Knowledge API       | 8081  |
| Orchestrator API    | 8082  |
| Import API          | 8083  |
| Export API          | 8084  |
| AI Orchestrator API | 8085  |
| Document Worker     | 8086  |
| RabbitMQ Management | 15672 |
| PostgreSQL          | 5432  |

---

# Access URLs

## Frontend

```
http://localhost:5000
```

---

## API Gateway

```
http://localhost:8080
```

---

## OpenAPI Documentation

Gateway Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

## RabbitMQ Management UI

```
http://localhost:15672
```

Default credentials:

```text
username: guest

password: guest
```

---

# Database Setup

The platform follows the **Database per Service** pattern.

Local databases:

| Service              | Database     |
| -------------------- | ------------ |
| Knowledge Service    | knowledge_db |
| Orchestrator Service | job_db       |
| Audit Service        | audit_db     |

Example:

```yaml
spring:

  datasource:

    url: jdbc:postgresql://postgres:5432/knowledge_db

    username: knowledge_user

    password: qwerty
```

---

# Database Initialization

Database schema is created automatically using:

* Spring Data JPA
* Hibernate migrations
* Flyway/Liquibase (recommended)

Example:

```yaml
spring:

  jpa:

    hibernate:
      ddl-auto: validate
```

---

# RabbitMQ Configuration

RabbitMQ is used for asynchronous communication.

Example queues:

```text
import.requested

import.completed

export.requested

export.completed

audit.events

notification.events
```

Configuration:

```yaml
spring:

  rabbitmq:

    host: rabbitmq

    port: 5672

    username: guest

    password: guest
```

---

# Running Frontend Locally

The UI can run independently.

Navigate:

```bash
cd ti-ui
```

Install dependencies:

```bash
npm install
```

Start development server:

```bash
npm run dev
```

Application:

```
http://localhost:5173
```

---

# Frontend Configuration

Example:

`.env`

```properties
VITE_API_URL=http://localhost:8080
```

The frontend communicates only with:

```text
React UI

   |

   v

ti-gateway-api
```

---

# Local Security Configuration

## Okta Integration

Local development uses Okta Hosted Login.

Gateway configuration:

```yaml
spring:

  security:

    oauth2:

      client:

        registration:

          okta:

            client-id: ${OKTA_CLIENT_ID}

            client-secret: ${OKTA_CLIENT_SECRET}


      provider:

        okta:

          issuer-uri:
            https://${OKTA_DOMAIN}/oauth2/default
```

---

# Local Environment Variables

Create:

```
.env
```

Example:

```properties
OKTA_DOMAIN=dev-xxxx.okta.com

OKTA_CLIENT_ID=xxxxx

OKTA_CLIENT_SECRET=xxxxx


DATABASE_PASSWORD=password

RABBITMQ_PASSWORD=password
```

Never commit `.env` files.

---

# Docker Compose Commands

## Start

```bash
docker compose up
```

---

## Start in Background

```bash
docker compose up -d
```

---

## Stop Environment

```bash
docker compose down
```

---

## Remove Volumes

Remove databases:

```bash
docker compose down -v
```

Warning:

This deletes local database data.

---

## View Logs

All services:

```bash
docker compose logs -f
```

Specific service:

```bash
docker compose logs -f ti-gateway-api
```

---

# Health Checks

Each Spring Boot service exposes:

```
/actuator/health
```

Example:

```
http://localhost:8080/actuator/health
```

Response:

```json
{
  "status": "UP"
}
```

---

# Debugging

## View Running Containers

```bash
docker ps
```

---

## Enter Container

Example:

```bash
docker exec -it ti-knowledge-api bash
```

---

## Check Database

Connect:

```bash
docker exec -it postgres psql -U postgres
```

List databases:

```sql
\l
```

---

# Common Issues

## Port Already Used

Error:

```
Bind for 0.0.0.0:8080 failed
```

Solution:

Find process:

```bash
lsof -i :8080
```

or change port mapping.

---

## Database Connection Failed

Check:

```bash
docker logs postgres
```

Verify:

* Database container is running
* Credentials match
* Network configuration is correct

---

## RabbitMQ Connection Failed

Check:

```bash
docker logs rabbitmq
```

Verify:

```yaml
spring.rabbitmq.host=rabbitmq
```

not:

```yaml
spring.rabbitmq.host=localhost
```

inside containers.

---

# Development Workflow

Recommended workflow:

```text
Developer

   |

   v

Run Infrastructure

docker compose up postgres rabbitmq


   |

   v

Start Backend Services


   |

   v

Start React UI


   |

   v

Develop and Test


   |

   v

Commit Changes
```

---

# Optional Local Observability

For production-like development, additional containers can be enabled:

* Prometheus
* Grafana
* OpenTelemetry Collector
* Loki

Example:

```text
Application

    |

    v

OpenTelemetry Collector

    |

    +------------+

    |            |

    v            v

Prometheus    Loki

    |

    v

Grafana
```

---

# Local Environment Best Practices

Follow these recommendations:

* Use Docker Compose for infrastructure dependencies.
* Keep service configuration externalized.
* Do not store secrets in Git.
* Use separate databases per service.
* Use the same Docker images locally and in CI/CD.
* Keep local setup close to production architecture.

---

# Troubleshooting Checklist

Before raising an issue:

✅ Docker Desktop is running
✅ Containers are healthy
✅ PostgreSQL is available
✅ RabbitMQ is available
✅ Environment variables are configured
✅ Okta configuration is valid
✅ Ports are not conflicting
✅ Application logs contain no errors

