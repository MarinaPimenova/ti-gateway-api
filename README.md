# Training Project - Training Internal (TI) Knowledge Platform

## Goal

Build a production-like cloud-native microservices application that demonstrates modern Java 
and Spring development practices commonly used in enterprise environments.

The application is an **Internal Knowledge Management System** designed to store, organize, import, 
and export technical interview questions, answers, learning resources, and code examples.

The primary purpose of this project is educational. 
It provides hands-on experience with modern backend development, frontend development, 
cloud-native architecture, security, event-driven communication, containerization, 
and deployment while following industry best practices.

---

# Learning Objectives

After completing this project you will have practical experience with:

- Java 21: Collections, Streams, Optional, Records, Pattern Matching, Virtual Threads
- Spring Boot 4: Spring Data JPA, Spring Security, Exception Handling
- React, Vite, Node.js
- PostgreSQL: CTE, Liquibase
- Event-driven Architecture, RabbitMQ
- OAuth2 / OpenID Connect, JWT, Okta
- REST APIs, OpenAPI / Swagger
- Docker, Docker Compose
- CI/CD, GitHub Actions
- AWS Deployment

- Microservices, Backend For Frontend (BFF) Pattern, API Gateway Pattern, Dependency Injection
- Observability
- Audit Logging
- Resilience4j
---

# Architecture

The platform follows:

- Microservices Architecture
- API Gateway + BFF Pattern
- Database per Service
- Event-Driven Architecture
- Asynchronous Processing
- Stateless Services

```mermaid
flowchart LR

    subgraph Frontends["Frontend Applications"]
        direction TB
        UI["TI Knowledge Dashboard<br/>React SPA<br/>Vite + Nginx"]
        AI_Chatbot_UI["AI Chatbot<br/>React SPA<br/>Vite + Nginx"]
    end

    Gateway["ti-gateway-api<br/>API Gateway + BFF"]

    subgraph APIs["Backend APIs"]
        direction TB
        Knowledge["ti-knowledge-api"]
        Export["ti-export-api"]
        Orchestrator["ti-orchestrator-api"]
        AI_Orchestrator["ti-ai-orchestrator-api"]
    end

    subgraph Messaging["Messaging"]
        direction TB
        Rabbit["RabbitMQ"]
    end

    subgraph Agents["AI Agents"]
        direction TB
        Document_Agent["ti-document-agent"]
        SQL_Agent["ti-sql-agent"]
    end

    subgraph Workers["Workers"]
        direction TB
        Import["ti-import-worker"]
        Document_Worker["ti-document-upload-worker"]
    end

    subgraph Databases["Databases"]
        direction TB
        Knowledge_DB["Knowledge DB<br/>PostgreSQL"]
        Document_DB["Document DB<br/>PostgreSQL + PGVector"]
        Assistant_DB["Assistant DB<br/>PostgreSQL"]
    end

    UI --> Gateway
    AI_Chatbot_UI --> Gateway

    Gateway --> Knowledge
    Gateway --> Export
    Gateway --> Orchestrator
    Gateway --> AI_Orchestrator

    Orchestrator --> Rabbit

    Rabbit --> Import
    Rabbit --> Document_Worker

    AI_Orchestrator --> Document_Agent
    AI_Orchestrator --> SQL_Agent

    Knowledge --> Knowledge_DB
    Export --> Knowledge_DB
    Import --> Knowledge_DB
    SQL_Agent --> Knowledge_DB

    Document_Worker --> Document_DB
    Document_Agent --> Document_DB

    AI_Orchestrator --> Assistant_DB
    Document_Agent --> Assistant_DB
    SQL_Agent --> Assistant_DB
```

---

# Microservices

| Service                | Responsibility                                             |
|------------------------|------------------------------------------------------------|
| ti-knowledge-ui        | TI Dashboard: React frontend, dashboard, user interactions |
| ti-ai-chatbot-ui       | AI Chatbot: React frontend, dashboard, user interactions   |
| ti-gateway-api         | API Gateway, BFF, OAuth2 Client, routing, security         |
| ti-knowledge-api       | Manage questions, answers, resources, code examples        |
| ti-orchestrator-api    | Manage long-running import/export workflows                |
| ti-import-worker       | Process Excel/CSV imports                                  |
| ti-export-api          | Generate export files                                      |
| ti-ai-orchestrator-api | Manage AI Chatbot                                          |
| ti-document-upload-worker     | Process (ETL pipeline) uploaded document                   |
| ti-document-agent      | Document AI Agent                                          |
| ti-sql-question-agent  | Question AI Agent                                          |

# Databases

| Database        | Responsibility                                                                      |
|-----------------|-------------------------------------------------------------------------------------|
| ti-knowledge-db | Store questions, their answers and their metadata such as:Tags,question level, etc. |
| ti-document-db  | AI Chatbot: Store embeddings of the uploaded documents                              |
| ti-assistant-db | AI Chatbot: Store users messages and the results of their processing                |

---

# Communication

## REST APIs

Used for synchronous operations:

* Authentication
* CRUD operations
* Search
* Dashboard requests
* Job status

## RabbitMQ Events

Used for asynchronous operations:

* Import processing
* Upload Document processing

Example:

```
ImportRequested

      ↓

Import Worker

      ↓

ImportCompleted

      ↓

UI is notified (SSE mechanism)
```

---

# Database Architecture

The platform follows the **Database per Service** pattern.

```
ti-knowledge-api
        |
        └── Knowledge Database


ti-document-upload-worker
        |
        └── Embeddings Database


```

Benefits:

* Service independence
* Loose coupling
* Independent evolution
* Better scalability

---

# Security

Authentication and authorization are implemented using:

* Okta
* OAuth2 Authorization Code Flow
* OpenID Connect
* JWT

Architecture:

```
Browser

   |
   | Session Cookie

   v

ti-gateway-api

   |
   | JWT Token

   v

Backend Services
```

Details:

* Gateway acts as OAuth2 Client
* Backend services act as OAuth2 Resource Servers

See:

* `docs/SECURITY.md`

---

# Technology Stack

## Backend

* Java 21
* Spring Boot 4
* Spring Web
* Spring Data JPA
* Spring Security
* Spring AMQP
* PostgreSQL

## Frontend

* React
* Vite
* Node.js
* Nginx

## Messaging

* RabbitMQ

## DevOps

* Docker
* Docker Compose
* GitHub Actions
* Kubernetes
* AWS

---

# Java Features Practiced

* Records
* Pattern Matching
* Switch Expressions
* Virtual Threads
* Streams
* Optional
* Collections
* Immutable Collections
* Functional Programming
* CompletableFuture

---

# Spring Features Practiced

* Dependency Injection
* Configuration Properties
* REST Controllers
* Validation
* Global Exception Handling
* Spring Data JPA
* OAuth2 Client
* OAuth2 Resource Server
* Spring AMQP

---

# Resilience

Implemented using Resilience4j:

* Circuit Breaker
* Retry
* Time Limiter
* Fallback

Used mainly in:

* API Gateway
* Orchestrator Service

---

# Observability

The platform supports:

* Micrometer
* OpenTelemetry
* Prometheus
* Grafana
* Structured JSON Logging

Collected data:

* HTTP metrics
* JVM metrics
* Database metrics
* RabbitMQ metrics
* Distributed traces

Trace information:

```
traceId
spanId
```

See:

* `docs/OBSERVABILITY.md`

---

# Auditability

Business actions are recorded through `ti-audit-api`.

Examples:

* User Login
* Question Created
* Question Updated
* Import Started
* Import Completed
* Export Started
* Export Completed

Audit information:

* User
* Timestamp
* Action
* Resource
* Status

---

# CI/CD

GitHub Actions pipeline:

```
Commit

 ↓

Build

 ↓

Tests

 ↓

Quality Checks

 ↓

Docker Build

 ↓

Publish Image

 ↓

Deploy
```

See:

* `docs/CI_CD.md`

---

# Local Development

The application can be started locally using Docker Compose.

Infrastructure:

* PostgreSQL
* RabbitMQ
* Prometheus
* Grafana

See:

* `docs/LOCAL_SETUP.md`

---

# AWS Deployment

The platform is AWS-ready.

Possible deployment:

```


Application Load Balancer

    ↓

Amazon EKS

    ↓

Microservices

    ↓

Amazon RDS PostgreSQL

    ↓

RabbitMQ

    ↓

CloudWatch / Grafana
```

See:

* `docs/AWS_DEPLOYMENT.md`

---

# Documentation

Additional documentation:

| Document          | Description                      |
| ----------------- | -------------------------------- |
| SECURITY.md       | Okta, OAuth2, JWT security model |
| CI_CD.md          | GitHub Actions pipeline          |
| LOCAL_SETUP.md    | Docker Compose environment       |
| AWS_DEPLOYMENT.md | AWS cloud deployment             |
| OBSERVABILITY.md  | Monitoring and tracing           |

---

# Project Goals

This project intentionally combines modern Java language features with enterprise architecture patterns.

After completing the implementation you will have a portfolio-ready microservices application demonstrating:

- Enterprise Security
- Modern Java Development
- Spring Boot Ecosystem
- Event-Driven Architecture
- Cloud-Native Design
- Frontend and Backend Integration
- Production-ready Deployment
- CI/CD Automation
- Observability
- Resilience
- Best Practices for Microservices

# Knowledge DB schema

```mermaid
erDiagram

    PROJECT {
        bigint id PK
        varchar project_name
        varchar project_lead
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    QUESTION_LEVEL {
        bigint id PK
        varchar code
        varchar description
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    KNOWLEDGE_CATEGORY {
        bigint id PK
        varchar category
        varchar description
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    KNOWLEDGE_TAG {
        bigint id PK
        bigint knowledge_category_id FK
        varchar tag
        varchar description
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    QUESTION {
        bigint id PK
        bigint question_level_id FK
        text question
        text short_answer
        text detailed_answer
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    RESOURCE {
        bigint id PK
        varchar description
        varchar resource_url
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    CODE_EXAMPLE {
        bigint id PK
        varchar language
        text source_code
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    QUESTION_TAG {
        bigint id PK
        bigint question_id FK
        bigint knowledge_tag_id FK
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    QUESTION_RESOURCE {
        bigint id PK
        bigint question_id FK
        bigint resource_id FK
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    QUESTION_CODE_EXAMPLE {
        bigint id PK
        bigint question_id FK
        bigint code_example_id FK
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    PROJECT_QUESTION {
        bigint id PK
        bigint project_id FK
        bigint question_id FK
        varchar created_by
        timestamptz created_date
        varchar updated_by
        timestamptz modified_date
    }

    QUESTION_LEVEL ||--o{ QUESTION : classifies

    KNOWLEDGE_CATEGORY ||--o{ KNOWLEDGE_TAG : contains

    QUESTION ||--o{ QUESTION_TAG : tagged
    KNOWLEDGE_TAG ||--o{ QUESTION_TAG : assigned

    QUESTION ||--o{ QUESTION_RESOURCE : references
    RESOURCE ||--o{ QUESTION_RESOURCE : linked

    QUESTION ||--o{ QUESTION_CODE_EXAMPLE : illustrates
    CODE_EXAMPLE ||--o{ QUESTION_CODE_EXAMPLE : reused

    PROJECT ||--o{ PROJECT_QUESTION : contains
    QUESTION ||--o{ PROJECT_QUESTION : belongs_to
```