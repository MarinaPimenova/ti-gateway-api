# Architecture

## Overview

The **TI Knowledge Platform** is a cloud-native microservices application 
that demonstrates modern enterprise architecture patterns using Java 21 and Spring Boot 4.

The system follows the principles of:

- Microservices Architecture
- API Gateway Pattern
- Backend For Frontend (BFF) Pattern
- Event-Driven Architecture (EDA)
- Database per Service
- Asynchronous Processing
- Stateless Services

The application consists of independent services that communicate using REST APIs 
and RabbitMQ events.

---

# High-Level Architecture

```mermaid
flowchart TB

    %% =========================================================
    %% Frontend Applications
    %% =========================================================

    subgraph UI["Frontend Applications"]
        direction LR

        KUI["ti-knowledge-ui<br/><br/>TI Platform Entry Point<br/>Dashboard / Base Information / Login"]

        CUI["ti-ai-chatbot-ui<br/><br/>AI Chatbot<br/>Chat / RAG"]

        QUI["ti-ai-question-ui<br/><br/>AI Resources Uploader<br/>Question Generation"]
    end


    %% =========================================================
    %% API Gateway
    %% =========================================================

    GW["ti-gateway-api<br/><br/>API Gateway / BFF<br/>OAuth2 Client / Security / Routing"]


    %% =========================================================
    %% Backend APIs
    %% =========================================================

    subgraph API["Backend APIs"]
        direction LR

        KA["ti-knowledge-api<br/><br/>Questions / Answers<br/>Resources / Code Examples"]

        ORCH["ti-orchestrator-api<br/><br/>Long-running<br/>Import / Export Workflows"]

        AIO["ti-ai-orchestrator-api<br/><br/>AI Chatbot Orchestration"]
    end


    %% =========================================================
    %% Workers / Processing
    %% =========================================================

    subgraph WORKERS["Workers / Processing"]
        direction LR

        IW["ti-import-worker<br/><br/>Excel / CSV Import"]

        DW["ti-document-worker<br/><br/>Document ETL / Processing<br/>Embeddings + Text Sections"]

        EA["ti-export-api<br/><br/>Export File Generation"]
    end


    %% =========================================================
    %% AI Agents
    %% =========================================================

    subgraph AGENTS["AI Agents"]
        direction LR

        DA["ti-document-agent<br/><br/>Document AI Agent<br/>RAG / Vector Search"]

        SA["ti-sql-question-agent<br/><br/>Question AI Agent<br/>SQL Generation / Knowledge DB"]
    end


    %% =========================================================
    %% Messaging
    %% =========================================================

    MQ[("RabbitMQ<br/><br/>Async Messaging")]


    %% =========================================================
    %% Databases
    %% =========================================================

    subgraph DB["Databases"]
        direction LR

        KDB[("ti-knowledge-db<br/><br/>Questions<br/>Answers<br/>Tags<br/>Question Levels<br/>Resources<br/>Code Examples")]

        DDB[("ti-document-db<br/><br/>Document Embeddings<br/>PGVector<br/>Document Text Sections")]

        ADB[("ti-assistant-db<br/><br/>User Messages<br/>Conversations<br/>AI Processing Results")]
    end


    %% =========================================================
    %% Frontend -> Gateway
    %% =========================================================

    KUI --> GW
    CUI --> GW
    QUI --> GW


    %% =========================================================
    %% Gateway -> Backend APIs
    %% =========================================================

    GW --> KA
    GW --> ORCH
    GW --> AIO


    %% =========================================================
    %% Knowledge API
    %% =========================================================

    KA --> KDB


    %% =========================================================
    %% Import / Export Workflows
    %% =========================================================

    ORCH -->|"Import / Export commands"| MQ

    MQ -->|"Import messages"| IW
    MQ -->|"Document processing messages"| DW

    IW -->|"Imported questions / data"| KDB

    ORCH --> EA


    %% =========================================================
    %% Document Upload / Processing
    %% =========================================================

    QUI -->|"Upload document"| GW
    GW -->|"Start document processing"| ORCH

    DW -->|"Document embeddings / vectors"| DDB
    DW -->|"Document text sections"| DDB


    %% =========================================================
    %% AI Chatbot Orchestration
    %% =========================================================

    AIO -->|"Document query"| DA
    AIO -->|"Question / SQL query"| SA

    AIO -->|"Conversations / messages / AI results"| ADB


    %% =========================================================
    %% Document AI Agent
    %% =========================================================

    DA -->|"Vector search / document retrieval"| DDB
    DA -->|"Conversation / processing context"| ADB


    %% =========================================================
    %% SQL Question AI Agent
    %% =========================================================

    SA -->|"SQL / question data"| KDB
    SA -->|"Conversation / processing context"| ADB
```

---

# Architecture Principles

## Single Responsibility

Each microservice owns a single business capability.

| Service                | Responsibility                           |
|------------------------|------------------------------------------|
| ti-knowledge-ui        | User Interface                           |
| ti-gateway-api         | API Gateway, BFF, Authentication         |
| ti-knowledge-api       | Knowledge management                     |
| ti-orchestrator-api    | Long-running job orchestration           |
| ti-import-worker       | Import processing                        |
| ti-export-api          | Export processing                        |
| ti-ai-orchestrator-api | Manage AI Chatbot                        |
| ti-document-worker     | Process (ETL pipeline) uploaded document |
| ti-document-agent      | Document AI Agent                        |
| ti-sql-agent           | Question AI Agent                        |

---

## Database per Service

Every microservice owns its own database.

This prevents tight coupling between services and enables independent deployment and evolution.

```
Knowledge Service
    │
    └── Knowledge Database


ti-document-worker
        |
        └── Embeddings Database
```

---

# Microservices

## ti-ui

Technology

- React
- Vite
- Node.js
- Nginx

Responsibilities

- Login
- Dashboard
- Question Editor
- Import page
- Export actions

The UI never communicates directly with backend services. All requests go through the API Gateway.

---

## ti-gateway-api

Patterns

- API Gateway
- Backend For Frontend (BFF)

Responsibilities

- Request routing
- API aggregation
- Security: OAuth2 Login, Okta Integration, JWT Validation
- Resilience4j

All frontend requests pass through this service.

---

## ti-knowledge-api

The core business service.

Responsibilities

- Manage Questions
- Manage Answers
- Manage Resources
- Manage Code Examples
- Search knowledge base

Provides synchronous REST APIs used by the Dashboard.

---

## ti-orchestrator-api

Coordinates asynchronous business operations.

Responsibilities

- Create Import Job
- Create Export Job
- Track Job Status
- Publish RabbitMQ events

The Dashboard communicates with this service to start long-running operations.

---

## ti-import-api

Processes uploaded Excel and CSV files.

Responsibilities

- Parse files
- Validate data
- Store imported knowledge
- Publish completion events

Import processing is asynchronous.

---

## ti-export-api

Generates downloadable files.

Responsibilities

- Read requested knowledge
- Generate CSV
- Generate Excel
- Publish completion events

---

Future extensions

- Email
- Slack
- Microsoft Teams

---

# Communication

The platform uses two communication styles.

## Synchronous Communication

REST APIs are used for:

- Authentication
- CRUD operations
- Searching questions
- Dashboard requests
- Job status

```
Browser

↓

Gateway

↓

Knowledge Service
```

---

## Asynchronous Communication

RabbitMQ is used for long-running operations.

Examples

- Import Worker
- Document Worker

```
orchestrator Service

↓

RabbitMQ

↓

Import Worker

↓

Knowledge Service

↓

ImportCompletedEvent

↓

UI is notified
```

---

# Event Flow

## Import

```text
User uploads file

        │

        ▼

Gateway

        │

        ▼

orchestrator Service

        │

ImportRequestedEvent

        │

        ▼

RabbitMQ

        │

        ▼

Import Service

        │

Validate and Import

        │

        ▼

Knowledge Database

        │

ImportCompletedEvent

        │

        ▼

UI is notified
```

---

# Why Event-Driven Architecture?

Importing and exporting large datasets may take several seconds or minutes.

Using asynchronous messaging provides several advantages:

- Better user experience
- Improved scalability
- Loose coupling
- Retry capabilities
- Independent service deployment
- Fault tolerance

CRUD operations remain synchronous because they are short-lived and require immediate feedback.

---

# Security Architecture

Authentication

- Okta

Authorization

- OAuth 2.0 Authorization Code Flow

Access Token

- JWT

Gateway

- OAuth2 Client

Backend Services

- OAuth2 Resource Servers

The Gateway validates user authentication before forwarding requests to backend services.

---

# Error Handling

Each service implements:

- Bean Validation
- Global Exception Handler
- Standard HTTP Error Responses
- Structured JSON Errors

Long-running jobs publish failure events instead of blocking user requests.

---

# Design Goals

The architecture demonstrates common enterprise development practices:

- Clear service boundaries
- Independent deployment
- Secure communication
- Asynchronous processing
- Event-driven workflows
- Cloud-ready design
- High maintainability
- Production-ready architecture