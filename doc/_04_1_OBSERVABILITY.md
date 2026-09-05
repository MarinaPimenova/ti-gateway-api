# Observability

## Overview

Observability enables developers to monitor application health, troubleshoot issues, and analyze system behavior.

This project implements a production-like observability stack using open-source technologies that can later be migrated to AWS with minimal configuration changes.

The observability solution includes:

* Spring Boot Actuator
* Micrometer
* Prometheus
* Grafana
* OpenTelemetry
* Zipkin
* Structured JSON Logging

---
### Observability tools

* **Micrometer** – the metrics library used in Spring Boot. It collects application metrics (HTTP requests, JVM, custom counters and timers) and exposes them to monitoring systems.

* **Prometheus** – a time-series database that scrapes metrics from applications (usually from the `/actuator/prometheus` endpoint) and stores them for querying with PromQL.

* **Grafana** – a visualization platform used to build dashboards and alerts from Prometheus metrics (and logs from Loki, if used).

* **OpenTelemetry** – the standard framework for collecting distributed traces, metrics, and logs. In Spring Boot, it propagates trace IDs across microservices and exports telemetry to a backend.

### Local environment: OpenTelemetry, Zipkin

**Keep OpenTelemetry**; **use Zipkin as the local tracing backend.**

They solve different problems:

* OpenTelemetry = instrumentation and **trace** **generation**.

* Zipkin = **trace storage** and visualization.

A typical local setup is:

`Spring Boot + Micrometer Tracing → OpenTelemetry → Zipkin`

---
Yes. For AWS EKS, the observability architecture is slightly different. The key point is that Fluent Bit is only for logs—it does not collect metrics or traces.

### Recommended AWS EKS architecture

![](blob\:https://chatgpt.com/6f469c99-20c7-4e7b-a3af-61cfa3138c0e)

Logs

stdout

Fluent Bit

CloudWatch Logs

Metrics

Actuator

Prometheus

AMP / Grafana

Traces

OpenTelemetry

OTel Collector

AWS X-Ray

### What each component does

|
Signal

|

Collected by

|

Stored in

|
| --- | --- | --- |
|

Logs

|

Fluent Bit

|

CloudWatch Logs

|
|

Metrics

|

Prometheus scraper

|

Amazon Managed Prometheus (AMP)

|
|

Dashboards

|

Grafana

|

Amazon Managed Grafana

|
|

Traces

|

OpenTelemetry

|

AWS X-Ray

|

### Dependencies for AWS EKS

You remove Zipkin and keep OpenTelemetry.

XML

<!-- Actuator -->

<dependency>

<groupId>org.springframework.boot</groupId>

<artifactId>spring-boot-starter-actuator</artifactId>

</dependency>

<!-- Prometheus metrics -->

<dependency>

<groupId>io.micrometer</groupId>

<artifactId>micrometer-registry-prometheus</artifactId>

</dependency>

<!-- Micrometer Tracing -> OpenTelemetry -->

<dependency>

<groupId>io.micrometer</groupId>

<artifactId>micrometer-tracing-bridge-otel</artifactId>

</dependency>

<!-- OTLP exporter (to OpenTelemetry Collector) -->

<dependency>

<groupId>io.opentelemetry</groupId>

<artifactId>opentelemetry-exporter-otlp</artifactId>

</dependency>

### Remove

XML

<!-- Local only -->

<dependency>

<groupId>io.opentelemetry</groupId>

<artifactId>opentelemetry-exporter-zipkin</artifactId>

</dependency>

That's the only dependency change.

### Why OTLP instead of X-Ray dependency?

The modern AWS recommendation is:

* Application → OTLP

* OpenTelemetry Collector → AWS X-Ray

Your application remains vendor-neutral and can run locally with Zipkin or in AWS without code changes.

### Environment-Specific Configuration

Use profiles rather than changing code.

**`application-local.yml`**

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
    zipkin:
      tracing:
        endpoint: http://zipkin:9411/api/v2/spans

```

**`application-aws.yml`**

```yaml
management:
  tracing:
    sampling:
      probability: 0.1
    otel:
      exporter:
        otlp:
          endpoint: http://otel-collector:4318

```

> **Note:** The OpenTelemetry Collector running in EKS exports to AWS X-Ray.

### Fluent Bit clarification

A common interview question is:

> What is Fluent Bit responsible for?

Answer: Fluent Bit is a lightweight log forwarder. It reads container stdout/stderr logs from Kubernetes nodes and sends them to CloudWatch Logs. It does not collect Prometheus metrics or distributed traces.

### Final dependency comparison

| Dependency | Local Docker | AWS EKS |
| --- | --- | --- |
| `spring-boot-starter-actuator` | ✅ | ✅ |
| `micrometer-registry-prometheus` | ✅ | ✅ |
| `micrometer-tracing-bridge-otel` | ✅ | ✅ |
| `opentelemetry-exporter-zipkin` | ✅ | ❌ |
| `opentelemetry-exporter-otlp` | ❌ | ✅ |

### Summary

* Local Docker: Use Zipkin as the tracing backend (`opentelemetry-exporter-zipkin`).

* AWS EKS: Replace Zipkin with the OTLP exporter (`opentelemetry-exporter-otlp`), which sends traces to the OpenTelemetry Collector and then to AWS X-Ray.

* The Actuator, Prometheus registry, and Micrometer Tracing dependencies remain the same in both environments.


---
# Architecture

```mermaid
flowchart LR

    User["React SPA"]

    Gateway["ti-gateway-api"]

    Knowledge["ti-knowledge-api"]

    Orchestrator["ti-orchestrator-api"]

    Import["ti-import-worker"]

    Export["ti-export-api"]

    AI_Orchestrator["ti-ai-orchestrator-api"]

    Document_Agent["ti-document-agent"]

    Prometheus["Prometheus"]

    Grafana["Grafana"]

    Zipkin["Zipkin"]

    User --> Gateway

    Gateway --> Knowledge
    Gateway --> Orchestrator

    Orchestrator --> Import
    Orchestrator --> Export

    Gateway --> Prometheus
    Knowledge --> Prometheus
    Orchestrator --> Prometheus
    Import --> Prometheus
    Export --> Prometheus
    AI_Orchestrator --> Prometheus
    Document_Agent --> Prometheus

    Gateway --> Zipkin
    Knowledge --> Zipkin
    Orchestrator --> Zipkin
    Import --> Zipkin
    Export --> Zipkin
    AI_Orchestrator --> Zipkin
    Document_Agent --> Zipkin

    Prometheus --> Grafana
```

---

# Components

## Spring Boot Actuator

Every backend service exposes operational endpoints used for monitoring.

Examples:

* `/actuator/health`
* `/actuator/info`
* `/actuator/prometheus`

---

## Micrometer

Micrometer (micrometer-registry-prometheus) collects application metrics and exposes them in a Prometheus-compatible format.

Examples:

* HTTP request count
* Response time
* JVM memory
* CPU usage
* Thread count
* Database connection pool
* RabbitMQ metrics

---

## Prometheus

Prometheus periodically collects metrics from all backend services.

Responsibilities:

* Metrics collection
* Time-series storage
* Query execution (PromQL)

---

## Grafana

Grafana visualizes application metrics using interactive dashboards.

Example dashboards:

* System Overview
* JVM Metrics
* HTTP Requests
* Database Metrics
* RabbitMQ Metrics
* Business Metrics

---

## OpenTelemetry

OpenTelemetry instruments distributed requests across microservices.

Every incoming request receives a unique trace identifier that is propagated between services.

- tracing-bridge-otel
- exporter-otlp
---

## Zipkin

Zipkin visualizes distributed traces and request execution paths.

Example trace:

```text
Browser

↓

ti-gateway-api

↓

ti-knowledge-api

↓

PostgreSQL
```

For asynchronous workflows:

```text
Browser

↓

ti-gateway-api

↓

ti-orchestrator-api

↓

RabbitMQ

↓

ti-import-api

↓

ti-knowledge-api
```

---

# Metrics

Each backend service exposes standard JVM and application metrics.

Typical metrics include:

## HTTP

* Request count
* Request duration
* Error rate
* Active requests

## JVM

* Heap memory
* Non-heap memory
* Garbage collection
* CPU usage
* Thread count

## Database

* Active connections
* Connection pool usage
* Query execution time

## RabbitMQ

* Published messages
* Consumed messages
* Queue depth
* Consumer count

---

# Business Metrics

Besides infrastructure metrics, the application records business-specific metrics.

Examples:

* Questions created
* Questions updated
* Questions deleted
* Import requests
* Successful imports
* Failed imports
* Export requests
* Successful exports
* Failed exports

These metrics provide insights into application usage and system activity.

---

# Distributed Tracing

Every request receives a unique:

* traceId
* spanId

Distributed tracing enables developers to:

* Follow requests across multiple services
* Measure response times
* Identify bottlenecks
* Troubleshoot failures

Example:

```text
GET /api/questions

traceId: 7f43...

ti-gateway-api

↓

ti-knowledge-api

↓

PostgreSQL
```

---

# Logging

All backend services produce structured JSON logs.

Each log entry includes:

* Timestamp
* Service name
* Log level
* traceId
* spanId
* Request ID
* User ID (when available)
* Message

Example:

```json
{
  "timestamp": "2026-08-01T10:15:30Z",
  "service": "ti-knowledge-api",
  "level": "INFO",
  "traceId": "7f43b6...",
  "spanId": "9ae1c2...",
  "message": "Question created successfully"
}
```

Using the trace identifier, logs can be correlated with Zipkin traces.

---

# Health Checks

Each microservice exposes health endpoints through Spring Boot Actuator.

Typical health indicators include:

* Application status
* Database connectivity
* RabbitMQ connectivity
* Disk space

These endpoints can later be integrated with Kubernetes readiness and liveness probes.

---

# Local Environment

The local development environment includes:

* PostgreSQL
* RabbitMQ
* Prometheus
* Grafana
* Zipkin

All components are started using Docker Compose.

---

# AWS Migration

The observability architecture can be migrated to AWS with minimal changes.

| Local         | AWS                                 |
| ------------- | ----------------------------------- |
| Prometheus    | Amazon Managed Prometheus           |
| Grafana       | Amazon Managed Grafana              |
| Zipkin        | AWS X-Ray                           |
| OpenTelemetry | AWS Distro for OpenTelemetry (ADOT) |
| JSON Logs     | Amazon CloudWatch Logs              |

Because observability is implemented using open standards, no business code changes are required during migration.

---

# Learning Objectives

After completing this module you will have practical experience with:

* Spring Boot Actuator
* Micrometer
* Prometheus
* Grafana
* OpenTelemetry
* Zipkin
* Distributed Tracing
* Structured Logging
* JVM Monitoring
* Application Metrics
* Business Metrics
* Health Checks
* Cloud-ready Observability
