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
flowchart TD

    UI["React SPA<br/>Vite + Nginx"]

    Gateway["ti-gateway-api<br/>API Gateway + BFF"]

    Knowledge["ti-knowledge-api"]

    Orchestrator["ti-orchestrator-api"]

    Import["ti-import-api"]

    Export["ti-export-api"]

    Audit["ti-audit-api"]

    Notification["ti-notification-api"]

    Rabbit["RabbitMQ"]

    DB["PostgreSQL Databases"]

    UI --> Gateway

    Gateway --> Knowledge
    Gateway --> Orchestrator

    Orchestrator --> Rabbit

    Rabbit --> Import
    Rabbit --> Export
    Rabbit --> Audit
    Rabbit --> Notification

    Knowledge --> DB
    Import --> DB
    Export --> DB
    Orchestrator --> DB
    Audit --> DB
````

---

# Microservices

| Service             | Responsibility                                      |
| ------------------- | --------------------------------------------------- |
| ti-ui               | React frontend, dashboard, user interactions        |
| ti-gateway-api      | API Gateway, BFF, OAuth2 Client, routing, security  |
| ti-knowledge-api    | Manage questions, answers, resources, code examples |
| ti-orchestrator-api | Manage long-running import/export workflows         |
| ti-import-api       | Process Excel/CSV imports                           |
| ti-export-api       | Generate export files                               |
| ti-audit-api        | Store business audit records                        |
| ti-notification-api | Process user notifications                          |

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
* Export processing
* Audit events
* Notifications

Example:

```
ImportRequested

      ↓

Import Service

      ↓

ImportCompleted

      ↓

Audit + Notification
```

---

# Database Architecture

The platform follows the **Database per Service** pattern.

```
ti-knowledge-api
        |
        └── Knowledge Database


ti-orchestrator-api
        |
        └── Job Database


ti-audit-api
        |
        └── Audit Database
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
CloudFront

    ↓

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