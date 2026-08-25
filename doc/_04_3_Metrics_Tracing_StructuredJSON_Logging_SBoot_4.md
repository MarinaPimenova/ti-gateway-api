Here is a complete guide 

to fulfilling all requirements for **Metrics**, **Distributed Tracing**, 

and **Structured JSON Logging** using Spring Boot.

---

## 1. Required Dependencies

Add the following to your `pom.xml`:

```xml
<dependencies>
    <!-- 1. Metrics & Prometheus -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    <!-- 2. Distributed Tracing (OTel Bridge + Zipkin Exporter) -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-otel</artifactId>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-zipkin</artifactId>
    </dependency>

    <!-- 3. Standard Spring Boot Starters (MVC/Web, RabbitMQ, PostgreSQL/JPA) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId> <!-- RabbitMQ Metrics auto-collected -->
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId> <!-- HikariCP Metrics auto-collected -->
    </dependency>
</dependencies>

```

---

## 2. Configuration (`application.yml`)

Configure **Tracing**, **Prometheus**, and **JSON Logging** in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: ti-knowledge-api # Automatically injected into JSON logs & traces

management:
  endpoints:
    web:
      exposure:
        include: prometheus, health, info # Exposes /actuator/prometheus
  metrics:
    tags:
      application: ${spring.application.name}
  tracing:
    enabled: true
    sampling:
      probability: 1.0 # 1.0 = Send 100% of traces to Zipkin (use 0.1 for 10% in production)
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans # Zipkin endpoint URL

# Enable Native JSON Structured Logging (Spring Boot 3.4+)
logging:
  structured:
    format:
      console: ecs # Emits logs in ECS JSON format containing service, traceId, spanId

```

---

## 3. How to Fulfill Your Metrics Requirements

Spring Boot Actuator + `micrometer-registry-prometheus` automatically captures almost all infrastructure metrics without extra code!

| Category | Desired Metric | How Spring Handles It Automatically |
| --- | --- | --- |
| **HTTP** | Request count, duration, active requests, errors | Captured automatically by Spring MVC (`http.server.requests`). |
| **JVM** | Heap, Non-heap, GC, CPU, Threads | Captured automatically by Micrometer (`jvm.memory.used`, `jvm.gc.pause`, `system.cpu.usage`). |
| **Database** | Active connections, pool usage | Captured automatically via **HikariCP** (`hikaricp.connections.active`, `hikaricp.connections.usage`). |
| **RabbitMQ** | Published, consumed, queue depth | Captured automatically by `spring-boot-starter-amqp` (`rabbitmq.published`, `rabbitmq.consumed`). |

### Registering Custom Business Metrics

For custom metrics (like **Questions created** or **Failed imports**), use `MeterRegistry` directly in your code:

```java
package com.wk.ti.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private final Counter questionCreatedCounter;
    private final Counter questionUpdatedCounter;

    public QuestionService(MeterRegistry registry) {
        // Register custom Prometheus counters
        this.questionCreatedCounter = registry.counter("business.questions.created");
        this.questionUpdatedCounter = registry.counter("business.questions.updated");
    }

    public void createQuestion() {
        // Business logic...
        
        // Increment metric
        questionCreatedCounter.increment();
    }
}

```

---

## 4. How Distributed Tracing Works

Once `micrometer-tracing-bridge-otel` is in your classpath and enabled in your `application.yml`:

1. Every incoming HTTP request automatically generates a `traceId` and `spanId`.
2. When calling another service using `RestClient` or `WebClient`, Spring automatically passes the trace headers (`traceparent`) along.
3. Spans are sent to Zipkin (`http://localhost:9411`) for visual tracing.

---

## 5. Structured JSON Logging with Trace Correlation

Spring Boot natively formats your console output into structured JSON. Micrometer Tracing injects the active `traceId` and `spanId` directly into the Logback MDC (Mapped Diagnostic Context).

### Injecting Contextual Custom Log Fields (Request ID & User ID)

To add `userId` or `requestId` to your JSON log entries, set them in the MDC via a Web Filter:

```java
package com.wk.ti.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            // Extract from headers or security context
            String userId = httpRequest.getHeader("X-User-ID");
            String requestId = httpRequest.getHeader("X-Request-Id");

            if (userId != null) MDC.put("userId", userId);
            if (requestId != null) MDC.put("requestId", requestId);

            chain.doFilter(request, response);
        } finally {
            // Clean up MDC after request finishes to avoid context leaks across thread pools
            MDC.remove("userId");
            MDC.remove("requestId");
        }
    }
}

```

### Resulting Output in Console

When you log an event inside your controller or service (`log.info("Question created successfully");`), Spring Boot emits formatted JSON:

```json
{
  "@timestamp": "2026-08-01T10:15:30.123Z",
  "log.level": "INFO",
  "message": "Question created successfully",
  "service.name": "ti-knowledge-api",
  "traceId": "7f43b612a842f109",
  "spanId": "9ae1c238f411b012",
  "requestId": "req-9912",
  "userId": "user-456"
}

```

---

## 6. Article Summary & Takeaways

1. **Metrics:** Exposed via HTTP endpoint `/actuator/prometheus` for Prometheus to pull.
2. **Tracing:** Handled automatically by Micrometer Tracing + OpenTelemetry bridge; exported via HTTP to Zipkin.
3. **Logging:** Built-in Spring Boot native structured logging automatically formats logs into machine-readable JSON and bridges active `traceId` and `spanId` straight into the JSON output.

---

If you are setting up Spring Boot Observability, this walkthrough provides a detailed step-by-step tutorial covering Actuator, Prometheus metrics, and OpenTelemetry tracing:

[Spring Boot 3 Observability: Tracing, Metrics, and Logging](https://www.google.com/search?q=https://www.youtube.com/watch%3Fv%3DJm2O04q3g34)

This video demonstrates how to configure trace propagation across microservices and link trace IDs directly into Prometheus and Zipkin dashboards.