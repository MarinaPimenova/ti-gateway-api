# Design Patterns

The **TI Knowledge Platform** demonstrates many of the classic **Gang of Four (GoF)** design patterns commonly used in enterprise Java applications.

## Creational Patterns

| Pattern | Definition | Possible Usage in TI Knowledge Platform |
|---------|------------|------------------------------------------|
| Singleton | Ensures that only one instance of a class exists throughout the application. | Spring Beans (`@Service`, `@Repository`, `@Component`) are singletons by default. |
| Factory Method | Creates objects without exposing the creation logic to the client. | Create Import or Export processors depending on the file type (CSV, Excel). |
| Abstract Factory | Creates families of related objects without specifying their concrete classes. | Create different exporter implementations (CSV, Excel, PDF). |
| Builder | Constructs complex objects step by step, improving readability and flexibility. | Build DTOs, REST responses. |

---

## Structural Patterns

| Pattern | Definition | Possible Usage in TI Knowledge Platform |
|---------|------------|------------------------------------------|
| Adapter | Converts one interface into another expected by the client. | Integrate Okta, RabbitMQ, Email, Slack, or Microsoft Teams APIs. |
| Facade | Provides a simplified interface to a complex subsystem. | `ti-gateway-api` hides the complexity of multiple backend microservices. |
| Proxy | Controls access to another object by acting as its representative. | Spring Security proxies, Spring AOP, Hibernate lazy-loading proxies. |
| Decorator | Adds responsibilities to an object dynamically without changing its class. | Add logging, auditing, caching, or resilience around business services. |
| Composite | Treats individual objects and groups of objects uniformly. | Represent hierarchical knowledge categories or documentation trees. |


---

## Behavioral Patterns

| Pattern | Definition | Possible Usage in TI Knowledge Platform                                         |
|---------|------------|---------------------------------------------------------------------------------|
| Strategy | Encapsulates interchangeable algorithms and selects one at runtime. | Choose Import, Export, Search strategies.                                       |
| Observer | Allows objects to be notified automatically when another object changes state. | RabbitMQ consumers react to ImportCompleted, ExportCompleted, and Audit events. |
| Command | Encapsulates a request as an object. | Import Job, Export Job, Delete Question commands.                               |
| Chain of Responsibility | Passes a request through a chain of handlers until one processes it. | Spring Security Filter Chain, Gateway filters, validation pipeline.             |
| State | Changes an object's behavior when its internal state changes. | Import/Export Job lifecycle (`PENDING → RUNNING → COMPLETED → FAILED`).         |
| Mediator | Centralizes communication between multiple objects to reduce coupling. | `ti-orchestrator-api` coordinates Import, Export services.                      |
| Iterator | Provides sequential access to collection elements without exposing the internal structure. | Java Collections and Stream API iterate over Questions and Resources.           |
| Visitor | Adds new operations to existing object structures without modifying them. | Export different domain objects using a common export mechanism.                |

---

# Enterprise Architecture Patterns

In addition to GoF design patterns, the platform demonstrates several enterprise architecture patterns.

| Pattern | Definition | Possible Usage                                                                     |
|---------|------------|------------------------------------------------------------------------------------|
| Microservices | Decomposes an application into independently deployable services. | Each business capability is implemented as an independent Spring Boot application. |
| API Gateway | Provides a single entry point for all client requests. | `ti-gateway-api` handles routing, security, and API aggregation.                   |
| Backend for Frontend (BFF) | Backend optimized specifically for one frontend application. | Gateway exposes APIs tailored for the React SPA.                                   |
| Database per Service | Each microservice owns its own database. | Knowledge, Job, and Audit services have independent PostgreSQL databases.          |
| Event-Driven Architecture (EDA) | Services communicate asynchronously using events. | RabbitMQ distributes Import, Document Upload events.                               |
| Publish-Subscribe | Publishers send events without knowing who consumes them. | Multiple services consume ImportCompleted and ExportCompleted events.              |
| CQRS (simplified) | Separates write operations from read operations or long-running processes. | CRUD operations remain synchronous while Import/Document Upload is asynchronous.   |
| Dependency Injection | Dependencies are provided by a framework rather than created manually. | Spring injects services, repositories, and configuration beans.                    |
| Repository | Encapsulates data access logic behind a collection-like interface. | Spring Data JPA repositories manage database operations.                           |
| DTO (Data Transfer Object) | Transfers data between application layers without exposing domain models. | REST APIs exchange DTOs instead of JPA entities.                                   |
| Service Layer | Encapsulates business logic in dedicated service classes. | Business rules are implemented in `@Service` classes.                              |
| MVC (Model-View-Controller) | Separates presentation, business logic, and data. | Spring MVC REST Controllers expose backend APIs.                                   |
| OAuth2 Client | Authenticates users using an external Identity Provider. | `ti-gateway-api` authenticates users through Okta Hosted Login.                    |
| Resource Server | Validates JWT access tokens before serving protected resources. | All backend microservices validate JWT tokens issued by Okta.                      |
| Circuit Breaker | Prevents repeated calls to failing services. | Resilience4j protects remote service communication.                                |
| Retry | Automatically retries transient failures. | Retry temporary failures when calling remote services or RabbitMQ.                 |
| Audit Logging | Records business actions for traceability and compliance. |                       |
| Health Check | Exposes application health for monitoring and orchestration. | Spring Boot Actuator provides `/actuator/health` endpoints for Kubernetes.         |