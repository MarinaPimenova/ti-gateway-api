# Rate Limiting

## Overview

The `ti-gateway-api` implements **Rate Limiting** to protect backend services from 
- excessive traffic, 
- accidental request flooding, 
- brute-force attacks, 
- and Denial-of-Service (DoS) attacks.

The implementation is based on the **Token Bucket** algorithm provided by **Bucket4j** and is implemented as a **Servlet Filter**.

Unlike an AOP-based implementation, 
the filter intercepts requests before they reach Spring MVC controllers or business logic, 
making it the preferred solution for an API Gateway.

---

# Goals

The Rate Limiting mechanism provides the following benefits:

- Protect backend services from overload.
- Prevent brute-force attacks.
- Reduce the impact of accidental request flooding.
- Limit abusive clients.
- Improve overall application stability.
- Reject excessive requests as early as possible.

---

# Architecture

```
                    HTTP Request
                         |
                         v
               +--------------------+
               | Spring Security    |
               +--------------------+
                         |
                         v
              +----------------------+
              | RateLimitingFilter   |
              +----------------------+
                         |
            +------------+-------------+
            |                          |
       Token available            No tokens
            |                          |
            v                          v
     Continue request           HTTP 429 Too Many Requests
            |
            v
      REST Controller
            |
            v
        Business Logic
```

---

# Token Bucket Algorithm

The implementation uses the **Token Bucket** algorithm.

A bucket contains a predefined number of tokens.

Each incoming request consumes one token.

When no tokens remain, the request is rejected with:

```
HTTP 429 Too Many Requests
```

After a configurable period, new tokens are automatically added to the bucket.

---

# Configuration

```yaml
rate:
  limiting:
    enabled: ${LP_RATE_LIMITING_ENABLED:true}
    capacity: ${LP_RATE_LIMITING_CAPACITY:100}
    refill: ${LP_RATE_LIMITING_REFILL:100}
    period: ${LP_RATE_LIMITING_PERIOD:1}   # minutes
```

## Configuration Parameters

| Property | Description | Default |
|----------|-------------|---------|
| enabled | Enables or disables Rate Limiting | true |
| capacity | Maximum number of tokens stored in a bucket | 100 |
| refill | Number of tokens added every refill period | 100 |
| period | Refill interval in minutes | 1 |

---

# Default Behaviour

Default configuration:

```yaml
capacity = 100
refill   = 100
period   = 1 minute
```

Result:

- Maximum burst size: **100 requests**
- Refill rate: **100 requests every minute**
- Sustained throughput: **100 requests/minute**

---

# Dependencies

```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j_jdk17-core</artifactId>
    <version>8.15.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

# Components

## RateLimitingProperties

Reads Rate Limiting configuration from `application.yml`.

Responsibilities:

- load configuration
- expose immutable configuration values

---

## RateLimiterService

Responsible for creating and managing Bucket4j buckets.

Responsibilities:

- create bucket for each client
- cache buckets
- configure Bucket4j limits

The service stores buckets in:

```
ConcurrentHashMap<String, Bucket>
```

The key represents an individual client.

---

## RateLimitingFilter

A Servlet Filter executed once for every HTTP request.

Responsibilities:

1. Identify the client.
2. Obtain the client's bucket.
3. Consume one token.
4. Continue processing if a token is available.
5. Return **HTTP 429** otherwise.

Example response headers:

```
HTTP/1.1 429 Too Many Requests

Retry-After: 42
```

---

# Client Identification

The implementation supports two identification strategies.

## Authenticated users

```
Authentication.getName()
```

Example:

```
john.doe
```

Each authenticated user receives an independent bucket.

---

## Anonymous users

When authentication is unavailable, the client IP address is used.

```
request.getRemoteAddr()
```

Example:

```
192.168.1.15
```

---

# Request Processing

```
Incoming Request
       |
       v
Is Rate Limiting enabled?
       |
       +---- No -----> Continue
       |
      Yes
       |
       v
Identify Client
       |
       v
Locate Bucket
       |
       v
Consume Token
       |
   +---+----+
   |        |
Success   Failure
   |        |
   |        |
   v        v
Continue   HTTP 429
```

---

# Why Servlet Filter Instead of Spring AOP?

| Servlet Filter | Spring AOP |
|----------------|------------|
| Executed before controllers | Executed after Spring MVC |
| Protects all endpoints | Only annotated methods |
| Suitable for API Gateway | Better for business logic |
| Lower overhead | Proxy overhead |
| Easy per-user/IP limiting | More difficult |
| Independent from controllers | Coupled with controller methods |

For an API Gateway, the Servlet Filter approach is the recommended solution.

---

# Production Considerations

The current implementation stores buckets in memory:

```
ConcurrentHashMap<String, Bucket>
```

This is suitable for:

- Local development
- Integration testing
- Single-instance deployments

However, each application instance maintains its own bucket cache.

Example:

```
          Gateway #1
             |
      ConcurrentHashMap

          Gateway #2
             |
      ConcurrentHashMap
```

With two gateway instances, a client could effectively perform approximately twice the configured request rate because each instance enforces its own limits independently.

---

# Distributed Deployment

For Kubernetes or multi-instance deployments, buckets should be stored in a distributed cache.

Recommended options:

- Redis
- Hazelcast
- Infinispan

Architecture:

```
                +-------------+
Client -------->| Gateway #1  |
                +-------------+
                       |
                       |
                  Redis Bucket
                       |
                +-------------+
Client -------->| Gateway #2  |
                +-------------+
```

A shared bucket store ensures that all gateway instances enforce the same rate limit.

---

# Recommendation for the Internal Knowledge Platform

For the Internal Knowledge Platform Gateway, the following approach is recommended.

### Development Environment

Use the in-memory implementation:

```
ConcurrentHashMap<String, Bucket>
```

Advantages:

- Simple
- No external infrastructure
- Easy debugging
- Excellent performance

---

### Production Environment

Replace the in-memory bucket cache with a Redis-backed Bucket4j implementation.

Advantages:

- Shared limits across all gateway instances
- Horizontal scalability
- Consistent behaviour in Kubernetes
- No changes required in the filter logic

---

### Client Identification Strategy

Use the following priority:

1. Authenticated user (`Authentication.getName()`)
2. Client IP address (`request.getRemoteAddr()`)

This provides fair limits for authenticated users while still protecting anonymous endpoints.

---

# Future Enhancements

Possible improvements include:

- Different limits for different API endpoints.
- Different limits for different user roles.
- Per-tenant rate limiting.
- Per-OAuth client rate limiting.
- Configurable limits loaded from a database.
- Response headers:
    - `X-Rate-Limit-Limit`
    - `X-Rate-Limit-Remaining`
    - `X-Rate-Limit-Reset`
- Monitoring using Spring Boot Actuator and Micrometer.
- Dashboard showing rejected requests and rate limiting statistics.

| Aspect                 | Current                    | Recommendation                                                   |
| ---------------------- | -------------------------- | ---------------------------------------------------------------- |
| Enable/disable         | ✅ Good                     | Keep                                                             |
| Capacity               | ✅ Good                     | Keep                                                             |
| Refill                 | ✅ Good                     | Keep                                                             |
| Excluded paths         | ❌ Missing                  | Add for actuator and Swagger endpoints                           |
| Key strategy           | ❌ Fixed in code            | Optional configuration if future flexibility is desired          |
| Per-user/IP support    | ✅ Supported by your filter | Keep                                                             |
| Distributed deployment | ⚠️ In-memory only          | Replace bucket storage with Redis for multi-instance deployments |
