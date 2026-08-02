# Security Architecture

## Overview

The **TI Knowledge Platform** uses a secure OAuth 2.0 / OpenID Connect (OIDC) architecture based on **Okta Hosted Login**, **Spring Security OAuth2 Client**, and **JWT-based resource server authentication**.

The security architecture separates authentication responsibilities:

- User authentication is delegated to Okta.
- The API Gateway manages the authenticated user session.
- Backend microservices validate JWT access tokens.
- Security events are captured through the audit service.

The platform follows:

- OAuth 2.0 Authorization Code Flow
- OpenID Connect
- Okta Hosted Login
- Session-based authentication between Browser and Gateway
- JWT-based authentication between Gateway and Microservices
- Spring Security OAuth2 Client
- Spring Security OAuth2 Resource Server
- Stateless backend services

---

# Security Architecture Overview

```mermaid
flowchart LR

    User["User"]

    Browser["React SPA<br/>Vite"]

    Okta["Okta Hosted Login<br/>OAuth2 + OIDC"]

    Gateway["ti-gateway-api<br/>OAuth2 Client<br/>Session Management"]

    Knowledge["ti-knowledge-api<br/>OAuth2 Resource Server"]

    Import["ti-import-api<br/>OAuth2 Resource Server"]

    Export["ti-export-api<br/>OAuth2 Resource Server"]

    Audit["ti-audit-api<br/>OAuth2 Resource Server"]

    User --> Browser

    Browser --> Gateway

    Gateway --> Okta

    Okta --> Gateway

    Gateway --> Knowledge
    Gateway --> Import
    Gateway --> Export
    Gateway --> Audit
````

---

# Authentication Architecture

## Authentication Responsibilities

| Component        | Responsibility                            |
| ---------------- | ----------------------------------------- |
| Okta             | User authentication and identity provider |
| React UI         | Initiates login request                   |
| API Gateway      | OAuth2 Client, session management         |
| Backend services | OAuth2 Resource Servers                   |

---

# Authentication Flow

The platform uses:

* OAuth 2.0 Authorization Code Flow
* OpenID Connect
* Okta Hosted Login Page

The user authentication flow:

1. User opens the React application.
2. React redirects the user to the Gateway login endpoint.
3. Gateway redirects the user to the Okta Hosted Login page.
4. User authenticates with Okta.
5. Okta redirects the browser back to the Gateway callback endpoint.
6. Gateway exchanges the authorization code for tokens.
7. Spring Security creates an authenticated local session.
8. Gateway sends a session cookie to the browser.
9. Browser uses the session cookie for subsequent requests.

---

# Authorization Code Flow

```text
+-------------+
| User        |
+------+------+
       |
       |
       v
+-------------+
| React SPA   |
+------+------+
       |
       |
       | /oauth2/authorization/okta
       |
       v
+----------------+
| Gateway        |
| OAuth2 Client  |
+----------------+
       |
       |
       | Redirect
       |
       v
+----------------+
| Okta Hosted    |
| Login Page     |
+----------------+
       |
       |
       | Authorization Code
       |
       v
+----------------+
| Gateway        |
| Callback       |
+----------------+
       |
       |
       | Token Exchange
       |
       v
+----------------+
| Okta Token     |
| Endpoint       |
+----------------+
       |
       |
       | Create Session
       |
       v
+----------------+
| Browser Cookie |
| JSESSIONID     |
+----------------+
```

---

# Browser to Gateway Security

## Session-Based Authentication

The communication between React UI and Gateway uses a secure server-side session.

Architecture:

```
React SPA

     |
     |
     | HTTPS
     |
     v

ti-gateway-api

     |
     |
     | HTTP Session
     |
     v

Authenticated User Context
```

The browser stores only:

```
JSESSIONID=<session-id>
```

The browser does not store:

* OAuth client secret
* Access token
* Refresh token

---


# Gateway OAuth2 Client Configuration

The Gateway is configured as an OAuth2 Client.

Responsibilities:

* Start authentication flow
* Redirect users to Okta
* Handle OAuth2 callback
* Exchange authorization code for tokens
* Create authenticated session
* Maintain user security context

Example:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          okta:
            client-id: ${OKTA_CLIENT_ID}
            client-secret: ${OKTA_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email

        provider:
          okta:
            issuer-uri: https://${OKTA_DOMAIN}/oauth2/default
```

---

# Gateway Security Configuration

Example:

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http) {

        return http
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/public/**")
                .permitAll()
                .anyExchange()
                .authenticated()
            )
            .oauth2Login(Customizer.withDefaults())
            .build();
    }
}
```

After successful login:

```
User
 |
 |
 v
Gateway Session Created

JSESSIONID Cookie

 |
 |
 v

Authenticated Requests
```

---

# Gateway to Microservices Security

Backend communication uses JWT-based authentication.

Architecture:

```
React SPA

   |
   |
   | Session Cookie
   |
   v

Gateway

   |
   |
   | JWT Access Token
   |
   v

Backend Services
```

Backend services are configured as:

* OAuth2 Resource Servers
* JWT token validators

---

# Resource Server Configuration

Each backend service:

* ti-knowledge-api
* ti-import-api
* ti-export-api
* ti-audit-api
* ti-notification-api

validates JWT tokens.

Example:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://${OKTA_DOMAIN}/oauth2/default
```

Spring Security validates:

* Token signature
* Issuer
* Expiration time
* Audience
* Claims

---

# JWT Token Example

Example access token payload:

```json
{
  "sub": "john.doe@example.com",
  "email": "john.doe@example.com",
  "groups": [
    "KNOWLEDGE_ADMIN"
  ],
  "scope": [
    "knowledge.read",
    "knowledge.write"
  ],
  "iss": "https://company.okta.com/oauth2/default",
  "exp": 1785600000
}
```

---

# Authorization Model

Authentication:

> Who is the user?

Authorization:

> What operations can the user perform?

The platform uses:

* Okta Groups
* OAuth2 scopes
* Spring Security authorities

Example:

| Role             | Permissions                    |
| ---------------- | ------------------------------ |
| KNOWLEDGE_USER   | Search knowledge               |
| KNOWLEDGE_EDITOR | Create and update questions    |
| KNOWLEDGE_ADMIN  | Manage knowledge configuration |
| EXPORT_USER      | Export data                    |

---

# API Gateway Responsibilities

The Gateway provides:

* Authentication entry point
* Session management
* JWT forwarding
* Request routing
* API aggregation
* Security enforcement
* Resilience handling

Frontend requests:

```
Browser

   |
   |
   | JSESSIONID Cookie
   |
   v

ti-gateway-api

   |
   |
   | Bearer JWT
   |
   v

Microservice
```

---

# Security Between Microservices

Internal communication follows zero-trust principles.

Each service validates incoming JWT tokens.

Example:

```
ti-orchestrator-api

       |
       |
       | Bearer Token
       |
       v

ti-import-api
```

Future improvements:

* OAuth2 Client Credentials Flow
* Service accounts
* Mutual TLS

---

# Logout Flow

Logout process:

1. User selects logout.
2. Gateway invalidates local session.
3. Browser session cookie is removed.
4. User is optionally redirected to Okta logout endpoint.

Flow:

```
Browser

 |
 |
 v

Gateway

 |
 |
 v

Invalidate Session

 |
 |
 v

Okta Logout Endpoint
```

---

# Security Headers

The Gateway applies:

* HTTPS enforcement
* Content Security Policy
* X-Content-Type-Options
* X-Frame-Options
* Strict Transport Security

Example:

```http
Strict-Transport-Security:
max-age=31536000

X-Content-Type-Options:
nosniff

X-Frame-Options:
DENY
```

---

# Secrets Management

Sensitive values are externalized:

* Okta client secret
* Database passwords
* RabbitMQ credentials

Recommended storage:

* AWS Secrets Manager
* Kubernetes Secrets
* Environment variables

Example:

```yaml
okta:
  client-secret: ${OKTA_CLIENT_SECRET}
```

---

# Audit Security Events

Security events are stored by:

```
ti-gateway-api

        |

        v

ti-audit-api
```

Examples:

| Event                   | Description                    |
| ----------------------- | ------------------------------ |
| USER_LOGIN              | Successful Okta authentication |
| USER_LOGOUT             | User logout                    |
| ACCESS_DENIED           | Authorization failure          |
| TOKEN_VALIDATION_FAILED | Invalid JWT                    |
| QUESTION_UPDATED        | Knowledge modification         |

---

# Security Monitoring

Security metrics:

* Login failures
* Authorization failures
* Invalid tokens
* Expired sessions
* Suspicious requests

Monitoring stack:

* Spring Boot Actuator
* OpenTelemetry
* Prometheus
* Grafana
* AWS CloudWatch

---

# Security Best Practices

## Authentication

Implemented:

* Centralized identity management
* Okta Hosted Login
* OAuth2 Authorization Code Flow
* Secure HTTP session

---

## Authorization

Implemented:

* OAuth2 scopes
* Okta groups
* Method-level security

---

## Data Protection

Implemented:

* HTTPS communication
* Secure cookies
* Externalized secrets
* Audit logging

---

# Future Security Enhancements

Possible improvements:

* Okta Adaptive MFA
* OAuth2 Client Credentials for service accounts
* Mutual TLS between services
* Fine-grained authorization policies
* SIEM integration (connecting a Security Information and Event Management platform to your IT and security tools to centralize log collection, normalize data formats, and correlate security events. Key aspects include data sources, normalization, and threat correlation.)



