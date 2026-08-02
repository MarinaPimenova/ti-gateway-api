# Observability

1. **Learn the observability concepts**, not a vendor.
2. **Avoid vendor lock-in** (Datadog, Dynatrace, New Relic, etc.).
3. **Be able to replace the backend with minimal code changes.**

The good news is that **Spring Boot 4 + Micrometer + OpenTelemetry** were designed exactly for this purpose.

---

# Recommendation

I would build the project around the following architecture.

| Concern         | Recommended Standard | Local Docker               | AWS EKS                   | Replaceable |
| --------------- | -------------------- | -------------------------- | ------------------------- | ----------- |
| Logs            | SLF4J + Logback      | ✅                          | ✅                         | Yes         |
| Metrics         | Micrometer           | ✅                          | ✅                         | Yes         |
| Traces          | OpenTelemetry        | ✅                          | ✅                         | Yes         |
| Dashboard       | Grafana              | ✅                          | ✅                         | Yes         |
| Metrics Storage | Prometheus           | ✅                          | Amazon Managed Prometheus | Yes         |
| Trace Storage   | Zipkin (local)       | AWS X-Ray / Tempo / Jaeger | Yes                       |             |

This stack is almost vendor-independent.

---

# Complete Observability Stack

| Layer             | Local Docker  | AWS                                 |
| ----------------- | ------------- | ----------------------------------- |
| Logging           | Logback       | CloudWatch                          |
| Metrics API       | Micrometer    | Micrometer                          |
| Metrics Collector | Prometheus    | Amazon Managed Prometheus           |
| Metrics Dashboard | Grafana       | Amazon Managed Grafana              |
| Tracing API       | OpenTelemetry | OpenTelemetry                       |
| Trace Collector   | Zipkin        | AWS Distro for OpenTelemetry (ADOT) |
| Trace Storage     | Zipkin        | X-Ray / Tempo                       |
| Alerting          | Grafana       | CloudWatch                          |

---

# Spring Initializer Dependencies

Let's review every dependency.

---

## 1. Prometheus ⭐⭐⭐⭐⭐

Purpose

Collects metrics.

Example

```
CPU

Memory

HTTP Requests

JVM Heap

GC

Connections

Response Time
```

Spring dependency

```
micrometer-registry-prometheus
```

Use?

> YES

Both

* Docker
* Kubernetes
* AWS

---

## 2. OpenTelemetry ⭐⭐⭐⭐⭐

Purpose

Distributed tracing.

Tracks one request across multiple services.

Example

```
UI

↓

Gateway

↓

Market API

↓

Analytics API

↓

Company API
```

Produces

```
TraceId

SpanId
```

Use?

Absolutely YES.

---

## 3. Zipkin ⭐⭐⭐

Only a Trace Collector.

Stores traces.

Great for Docker Desktop.

Not recommended for production AWS anymore.

Use?

Docker only.

---

## 4. Datadog ⭐⭐⭐⭐

Commercial SaaS.

Provides

* metrics
* logs
* traces
* dashboards
* alerting

Very little coding.

Expensive.

Use?

Real enterprise.

Not training.

---

## 5. Dynatrace ⭐⭐⭐⭐

Same idea.

Commercial.

Enterprise monitoring.

---

## 6. New Relic ⭐⭐⭐⭐

Another SaaS.

Very similar.

---

## 7. Graphite ⭐⭐

Old metrics database.

Before Prometheus existed.

Rarely used today.

Skip.

---

## 8. InfluxDB ⭐⭐⭐

Time-series database.

Excellent.

Prometheus is usually preferred with Spring Boot.

Skip for this project.

---

## 9. OTLP ⭐⭐⭐⭐⭐

OTLP

=

OpenTelemetry Protocol

Not a monitoring tool.

It's a protocol.

Think of it as

```
HTTP

for

Telemetry
```

OpenTelemetry exports

↓

OTLP

↓

Collector

↓

Prometheus

↓

Grafana

---

# What does Micrometer do?

Micrometer is NOT Prometheus.

Micrometer is an abstraction.

```
Application

↓

Micrometer

↓

Prometheus

or

Datadog

or

CloudWatch

or

Influx

or

New Relic
```

This is why Spring Boot uses it.

---

# What does OpenTelemetry do?

Exactly the same idea.

```
Application

↓

OpenTelemetry API

↓

Zipkin

Jaeger

AWS X-Ray

Tempo

Datadog

Dynatrace
```

Again

Vendor independent.

---

# Logging

Spring Boot already gives

```
SLF4J

+

Logback
```

Do NOT replace it.

Instead configure JSON logs.

Example

```json
{
  "timestamp":"...",
  "traceId":"...",
  "spanId":"...",
  "service":"market-api",
  "level":"INFO",
  "message":"Stock calculated"
}
```

Then

Docker

↓

stdout

↓

CloudWatch

or Loki

or ELK

No code changes.

---

# What should be implemented manually?

This is actually where you'll learn the most.

## Custom Metrics

Instead of relying only on JVM metrics.

Example

```
stock.search.count

recommendation.count

third.party.failures

recommendation.duration

stock.cache.hit

stock.cache.miss

login.success

login.failure
```

---

Example

```java
Counter recommendationCounter;

Timer recommendationTimer;

Gauge cacheSize;
```

---

## Business Metrics

Much more valuable.

```
Average Recommendation Score

Average API Latency

Stocks Calculated

Top Requested Symbol

Portfolio Size

Active Users
```

---

## Custom Logging

Instead of

```
logger.info("Started");
```

Use

```
logger.info(
    "Recommendation calculated for {} score {}",
    symbol,
    score
);
```

Include

```
traceId

userId

duration

requestId
```

---

## Custom Traces

Instead of automatic spans only.

Create spans manually.

```
Recommendation Calculation

↓

Load Market Data

↓

Load Company Data

↓

Call News API

↓

Calculate Score

↓

Save Audit
```

This gives a beautiful timeline.

---

# Suggested Dependencies

## Every Microservice

```
Spring Web

Spring Security

Spring Data JPA

Validation

PostgreSQL Driver

Actuator

RabbitMQ
```

---

## Metrics

```
micrometer-registry-prometheus
```

---

## Tracing

```
micrometer-tracing

opentelemetry
```

---

## Logging

Default Logback

---

# Local Docker

```
React

↓

Gateway

↓

Services

↓

Postgres

RabbitMQ

↓

Prometheus

↓

Grafana

↓

Zipkin
```

Very easy.

---

# AWS

```
Application

↓

OpenTelemetry

↓

ADOT Collector

↓

Amazon Managed Prometheus

↓

Amazon Managed Grafana

↓

CloudWatch Logs

↓

AWS X-Ray
```

Exactly the same application code.

---

# Final Recommendation

If I were designing this as a portfolio project for interviews, I would deliberately **separate the observability API from the observability backend**. In practice, that means:

| Area        | Code against                               | Local backend         | AWS backend                                 | Future replacement effort |
| ----------- | ------------------------------------------ | --------------------- | ------------------------------------------- | ------------------------- |
| Logging     | SLF4J + Logback                            | Console / Docker logs | CloudWatch Logs                             | None                      |
| Metrics     | Micrometer                                 | Prometheus + Grafana  | Amazon Managed Prometheus + Managed Grafana | None                      |
| Tracing     | OpenTelemetry API (via Micrometer Tracing) | Zipkin                | AWS Distro for OpenTelemetry (ADOT) → X-Ray | None                      |
| Correlation | TraceId / SpanId in MDC                    | Automatic             | Automatic                                   | None                      |

This approach lets you **write observability code once** while switching between Docker Desktop, AWS, or commercial platforms like Datadog or Dynatrace simply by changing configuration and dependencies rather than business code.

### A simple learning progression

For your training project, I'd recommend implementing observability in stages:

1. **Stage 1:** Spring Boot Actuator + Micrometer + Prometheus (basic JVM and HTTP metrics).
2. **Stage 2:** Add **custom business metrics** (`Counter`, `Timer`, `Gauge`) for stock searches, recommendation calculations, cache hits/misses, etc.
3. **Stage 3:** Add **OpenTelemetry tracing**, including manual spans around the recommendation workflow.
4. **Stage 4:** Configure **structured JSON logging** with `traceId`, `spanId`, `userId`, and request duration.
5. **Stage 5:** Swap the backend from **Prometheus + Zipkin** (Docker) to **Amazon Managed Prometheus + ADOT + CloudWatch/X-Ray** (EKS) without changing application code.

