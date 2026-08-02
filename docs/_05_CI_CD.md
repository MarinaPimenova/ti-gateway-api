
# CI/CD Architecture

## Overview

The **TI Knowledge Platform** uses a modern Continuous Integration and Continuous Delivery (CI/CD) approach based on **GitHub Actions**.

The CI/CD pipeline automates:

- Source code validation
- Build process
- Unit and integration testing
- Static code analysis
- Docker image creation
- Security scanning
- Artifact publishing
- Deployment automation

The pipeline supports the cloud-native microservices architecture:

- React + Vite frontend
- Spring Boot 4 backend services
- PostgreSQL databases
- RabbitMQ messaging
- Docker containers
- Kubernetes deployment

---

# CI/CD Architecture Overview

```mermaid
flowchart LR

    Developer["Developer"]

    GitHub["GitHub Repository"]

    Actions["GitHub Actions"]

    Build["Build & Test"]

    Quality["Quality Checks"]

    Docker["Docker Build"]

    Registry["Container Registry"]

    Deploy["Deployment"]

    Kubernetes["Kubernetes Cluster"]

    Monitoring["Observability"]

    Developer --> GitHub

    GitHub --> Actions

    Actions --> Build

    Build --> Quality

    Quality --> Docker

    Docker --> Registry

    Registry --> Deploy

    Deploy --> Kubernetes

    Kubernetes --> Monitoring
````

---

# Repository Structure

The project follows a multi-module repository structure.

Example:

```
ti-knowledge-platform
│
├── ti-ui
│   ├── React
│   ├── Vite
│   └── Dockerfile
│
├── ti-gateway-api
│   ├── Spring Boot 4
│   └── Dockerfile
│
├── ti-knowledge-api
│   ├── Spring Boot 4
│   └── Dockerfile
│
├── ti-orchestrator-api
│   ├── Spring Boot 4
│   └── Dockerfile
│
├── ti-import-api
│
├── ti-export-api
│
├── ti-audit-api
│
├── ti-notification-api
│
├── docs
│
└── .github
    └── workflows
```

---

# GitHub Actions Workflow Structure

CI/CD workflows are stored under:

```
.github/workflows
```

Example:

```
.github
└── workflows
    |
    ├── ci-backend.yml
    ├── ci-frontend.yml
    ├── docker-build.yml
    └── deploy.yml
```

---

# Pipeline Stages

The CI/CD pipeline consists of the following stages:

```text
Developer Commit

        |

        v

Pull Request Validation

        |

        v

Build

        |

        v

Unit Tests

        |

        v

Integration Tests

        |

        v

Static Analysis

        |

        v

Docker Image Build

        |

        v

Security Scan

        |

        v

Publish Image

        |

        v

Deploy Environment
```

---

# Backend CI Pipeline

Backend services use:

* Java 21
* Spring Boot 4
* Maven
* JUnit 5
* Testcontainers

Pipeline steps:

1. Checkout source code
2. Configure Java 21
3. Restore Maven cache
4. Build application
5. Execute tests
6. Generate test reports
7. Package application

---

## Example Backend Workflow

`.github/workflows/ci-backend.yml`

```yaml
name: Backend CI

on:
  pull_request:
    branches:
      - main

  push:
    branches:
      - main


jobs:

  build:

    runs-on: ubuntu-latest

    steps:

      - name: Checkout source
        uses: actions/checkout@v4


      - name: Setup Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: temurin
          cache: maven


      - name: Build application
        run: |
          mvn clean verify


      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/target/surefire-reports'
```

---

# Frontend CI Pipeline

Frontend technology stack:

* React
* Vite
* Node.js
* npm

Pipeline steps:

1. Install dependencies
2. Run lint checks
3. Execute frontend tests
4. Build production bundle

Example:

```yaml
name: Frontend CI

on:

  pull_request:
    branches:
      - main


jobs:

  frontend-build:

    runs-on: ubuntu-latest


    steps:

      - uses: actions/checkout@v4


      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: npm


      - name: Install dependencies
        run: |
          npm ci
        working-directory: ti-ui


      - name: Run tests
        run: |
          npm test
        working-directory: ti-ui


      - name: Build application
        run: |
          npm run build
        working-directory: ti-ui
```

---

# Code Quality Checks

The pipeline validates code quality using:

* Maven Checkstyle
* SonarQube
* ESLint
* Dependency vulnerability scanning

Example:

```text
Build

 |

 v

Unit Tests

 |

 v

SonarQube Analysis

 |

 v

Quality Gate
```

Quality gates verify:

* Code coverage
* Security issues
* Code smells
* Vulnerabilities

---

# Docker Image Build

Each microservice contains its own Dockerfile.

Example:

```
ti-knowledge-api

    |
    |
    v

Dockerfile

    |
    |
    v

Container Image
```

Example Dockerfile:

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT [
 "java",
 "-jar",
 "app.jar"
]
```

---

# Docker Build Workflow

Example:

```yaml
name: Docker Build

jobs:

  docker:

    runs-on: ubuntu-latest

    steps:

      - uses: actions/checkout@v4


      - name: Build image
        run: |
          docker build \
          -t ti-knowledge-api:${{ github.sha }} .
```

---

# Container Registry

Docker images are published to:

Possible registries:

* GitHub Container Registry (GHCR)
* Amazon Elastic Container Registry (ECR)

Example image:

```
ghcr.io/company/ti-knowledge-api:1.0.0
```

---

# Security Scanning

The pipeline performs security validation:

## Dependency Scanning

Checks:

* Maven dependencies
* npm packages

Tools:

* Dependabot
* OWASP Dependency Check

## Container Scanning

Checks:

* Base image vulnerabilities
* Package vulnerabilities
* Known CVEs

Example:

```text
Docker Image

      |

      v

Security Scanner

      |

      v

Pass / Fail
```

---

# Deployment Pipeline

Deployment is automated after successful CI validation.

Deployment flow:

```text
GitHub Actions

        |

        v

Container Registry

        |

        v

Kubernetes Deployment

        |

        v

Application Pods
```

---

# Kubernetes Deployment

The platform is deployed using:

* Kubernetes
* Helm Charts
* Docker images

Example:

```
Kubernetes Namespace

|
|
├── ti-ui
|
├── ti-gateway-api
|
├── ti-knowledge-api
|
├── ti-orchestrator-api
|
├── ti-import-api
|
├── ti-export-api
|
├── ti-audit-api
|
└── ti-notification-api
```

---

# Environment Strategy

The platform supports multiple environments:

| Environment | Purpose                     |
| ----------- | --------------------------- |
| Development | Developer testing           |
| Test        | Automated validation        |
| UAT         | Business acceptance testing |
| Production  | Live environment            |

---

# Configuration Management

Environment-specific configuration is externalized.

Examples:

* Database URLs
* Okta configuration
* RabbitMQ connection
* AWS settings

Configuration sources:

* Kubernetes ConfigMaps
* Kubernetes Secrets
* AWS Secrets Manager

Example:

```yaml
spring:

  datasource:
    url: ${DATABASE_URL}

  security:
    oauth2:
      client:
        registration:
          okta:
            client-id: ${OKTA_CLIENT_ID}
```

---

# Deployment Strategies

Supported deployment approaches:

## Rolling Deployment

Default Kubernetes strategy.

Advantages:

* Zero downtime
* Gradual rollout
* Automatic replacement

## Blue-Green Deployment

Future enhancement.

Advantages:

* Instant rollback
* Safer releases

---

# Rollback Strategy

If deployment fails:

1. Kubernetes detects unhealthy pods.
2. Previous version remains available.
3. Deployment is rolled back.

Example:

```text
Version 1.0

      |

Deploy

      |

Version 1.1 FAILED

      |

Rollback

      |

Version 1.0 RESTORED
```

---

# Git Workflow

Recommended workflow:

```text
feature branch

       |

       v

Pull Request

       |

       v

CI Validation

       |

       v

Code Review

       |

       v

Merge Main

       |

       v

Deployment Pipeline
```

---

# Branch Protection Rules

The main branch requires:

* Pull Request approval
* Successful CI pipeline
* Code review
* Security checks passed

---

# Observability Integration

Deployment pipelines integrate with:

* OpenTelemetry
* Prometheus
* Grafana
* AWS CloudWatch

Deployment events are monitored:

* Deployment status
* Application health
* Error rates
* Performance metrics

---

# Production Readiness Checklist

Before production deployment:

✅ Build successful
✅ Unit tests passed
✅ Integration tests passed
✅ Security scans passed
✅ Docker image created
✅ Configuration validated
✅ Database migrations executed
✅ Kubernetes deployment successful
✅ Monitoring enabled

---

# Future CI/CD Improvements

Possible enhancements:

* GitHub Actions reusable workflows
* Automated Helm deployments
* Terraform infrastructure deployment
* AWS EKS deployment pipeline
* Canary releases
* Automated performance testing
* Infrastructure security scanning
* GitOps approach using ArgoCD


