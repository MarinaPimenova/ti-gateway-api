
# Local Development Environment

## Overview

The **TI Knowledge Platform** provides a complete local development environment using **Docker Compose**.

The local environment allows developers to run the complete microservices platform on a developer workstation without requiring external cloud infrastructure.

The local setup includes:

- React + Vite frontend
- Spring Boot 4 microservices
- PostgreSQL databases
- RabbitMQ message broker
- Okta development integration
- Observability components (optional)

---

# Local Architecture

```mermaid
flowchart LR

    Browser["Browser"]

    UI["ti-ui<br/>React + Vite"]

    Gateway["ti-gateway-api"]

    Knowledge["ti-knowledge-api"]

    Orchestrator["ti-orchestrator-api"]

    Import["ti-import-api"]

    Export["ti-export-api"]

    Audit["ti-audit-api"]

    Notification["ti-notification-api"]

    Rabbit["RabbitMQ"]

    PG1["Knowledge DB"]

    PG2["Job DB"]

    PG3["Audit DB"]


    Browser --> UI

    UI --> Gateway

    Gateway --> Knowledge
    Gateway --> Orchestrator

    Orchestrator --> Rabbit

    Rabbit --> Import
    Rabbit --> Export
    Rabbit --> Audit
    Rabbit --> Notification

    Knowledge --> PG1
    Import --> PG1
    Export --> PG1

    Orchestrator --> PG2

    Audit --> PG3
````

---

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

# Project Structure

Example repository structure:

```text
ti-knowledge-platform

│
├── docker-compose.yml
│
├── ti-ui
│   ├── package.json
│   └── Dockerfile
│
├── ti-gateway-api
│   ├── pom.xml
│   └── Dockerfile
│
├── ti-knowledge-api
│
├── ti-orchestrator-api
│
├── ti-import-api
│
├── ti-export-api
│
├── ti-audit-api
│
├── ti-notification-api
│
├── postgres
│
└── rabbitmq
```

---

# Docker Compose Overview

The local environment is started using:

```bash
docker compose up
```

Docker Compose starts:

* Backend microservices
* Databases
* RabbitMQ
* Supporting infrastructure

Example:

```yaml
services:

  postgres:
    image: postgres:17

  rabbitmq:
    image: rabbitmq:4-management

  ti-gateway-api:
    build:
      context: ./ti-gateway-api

  ti-knowledge-api:
    build:
      context: ./ti-knowledge-api
```

---

# Starting the Environment

## Clone Repository

```bash
git clone <repository-url>

cd ti-knowledge-platform
```

---

## Build Backend Services

Build all Spring Boot applications:

```bash
mvn clean package
```

The build creates:

```text
target/*.jar
```

---

## Build Docker Images

Build all application images:

```bash
docker compose build
```

Example images:

```text
ti-ui

ti-gateway-api

ti-knowledge-api

ti-orchestrator-api

ti-import-api

ti-export-api

ti-audit-api

ti-notification-api
```

---

## Start Infrastructure

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
| ------------------- | ----- |
| React UI            | 3000  |
| Gateway API         | 8080  |
| Knowledge API       | 8081  |
| Orchestrator API    | 8082  |
| Import API          | 8083  |
| Export API          | 8084  |
| Audit API           | 8085  |
| Notification API    | 8086  |
| RabbitMQ Management | 15672 |
| PostgreSQL          | 5432  |

---

# Access URLs

## Frontend

```
http://localhost:3000
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

