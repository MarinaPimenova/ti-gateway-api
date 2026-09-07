# Generate REST API Contracts for TI Microservices

You are working in the local workspace:

```text
~/ti-2026
```

Your task is to crawl the backend microservice repositories under this directory and generate REST API contract documentation for each included microservice.

**Do not modify application source code.**

The only files you should create or update are the generated Markdown API-contract documentation files under each microservice's `doc` directory.

---

# 1. Repositories to crawl

Crawl the following repositories under `~/ti-2026`:

| Repository               | Microservice name |
| ------------------------ | ----------------- |
| `ti-gateway-api`         | `gateway`         |
| `ti-knowledge-api`       | `knowledge`       |
| `ti-orchestrator-api`    | `orchestrator`    |
| `ti-import-worker`       | `import`          |
| `ti-export-api`          | `export`          |
| `ti-ai-orchestrator-api` | `ai-orchestrator` |
| `ti-document-worker`     | `document-worker` |
| `ti-document-agent`      | `document-agent`  |

The following repository must be **explicitly skipped**:

| Repository     | Reason                                           |
| -------------- | ------------------------------------------------ |
| `ti-sql-agent` | Explicitly excluded from API-contract generation |

Also skip any repository/directory under `~/ti-2026` whose name:

* ends with `db`;
* ends with `ui`.

Do not generate documentation for these excluded repositories/directories.

---

# 2. Repository → microservice name mapping

Use the following mapping when constructing REST API URLs.

Do **not** infer or change these names.

```text
ti-gateway-api       -> gateway
ti-knowledge-api     -> knowledge
ti-orchestrator-api  -> orchestrator
ti-import-worker     -> import
ti-export-api        -> export
ti-ai-orchestrator-api -> ai-orchestrator
ti-document-worker   -> document-worker
ti-document-agent    -> document-agent
ti-sql-agent         -> sql-agent
```

`ti-sql-agent` is still excluded and must not be crawled.

---

# 3. Output files

Generate **one Markdown file per microservice**.

The output file must be created inside the corresponding repository:

```text
<repository>/doc/<microservice-name>-api-contract.md
```

Therefore, the expected files are:

```text
~/ti-2026/ti-gateway-api/doc/gateway-api-contract.md
~/ti-2026/ti-knowledge-api/doc/knowledge-api-contract.md
~/ti-2026/ti-orchestrator-api/doc/orchestrator-api-contract.md
~/ti-2026/ti-import-worker/doc/import-api-contract.md
~/ti-2026/ti-export-api/doc/export-api-contract.md
~/ti-2026/ti-ai-orchestrator-api/doc/ai-orchestrator-api-contract.md
~/ti-2026/ti-document-worker/doc/document-worker-api-contract.md
~/ti-2026/ti-document-agent/doc/document-agent-api-contract.md
```

If the `doc` directory does not exist, create it.

If the target Markdown file already exists, update/regenerate it with the current information.

Do not create a single combined API-contract file.

---

# 4. Discover controllers

For each included repository, inspect the source code and find all Java classes annotated with:

```java
@RestController
```

For every such controller, inspect all methods annotated with:

```java
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
```

Also consider composed/custom Spring MVC mapping annotations if they clearly represent one of these HTTP operations.

For each endpoint, inspect:

```java
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@PathVariable
@RequestParam
@RequestBody
```

as well as:

* controller method return type;
* DTO classes;
* request/response classes;
* `ResponseEntity`;
* service/processor methods when necessary to understand the endpoint behavior;
* relevant enums and projections when necessary to understand request/response structures.

The generated documentation must describe the **actual implementation found in the repository**.

---

# 5. Do not miss endpoints

Every matching controller endpoint must be documented.

Do not:

* omit endpoints because they appear trivial;
* add endpoints that do not exist;
* invent HTTP methods;
* invent URL paths;
* invent request fields;
* invent response fields;
* invent HTTP status codes.

If information cannot be determined reliably from the source code, explicitly write:

```text
Cannot be determined from source code
```

instead of guessing.

---

# 6. Constructing the REST API URL

The REST API URL must include the microservice name.

For example, suppose `ti-knowledge-api` contains:

```java
@RequestMapping("/api/v1/questions")
```

The path should be interpreted as:

```text
/api/v1
/questions
```

The microservice name `knowledge` must be inserted between these two parts:

```text
/api/v1/knowledge/questions
```

Therefore:

```text
<server address>/api/v1/knowledge/questions
```

If the controller method contains:

```java
@GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
```

the complete endpoint becomes:

```text
GET <server address>/api/v1/knowledge/questions/search
```

Use this general transformation:

```text
@RequestMapping("/api/v1/<resource>")
```

becomes:

```text
<server address>/api/v1/<microservice-name>/<resource>
```

For example:

```text
/api/v1/questions
        ↓
/api/v1/knowledge/questions
```

and:

```text
/api/v1/projects
        ↓
/api/v1/knowledge/projects
```

For other microservices, use their mapped microservice name.

Example:

```text
/api/v1/import/...
```

for `ti-import-worker`.

---

# 7. Important URL rule

Preserve the actual controller and method mapping structure from the source code.

Do not arbitrarily rewrite paths.

The only intentional transformation is inserting the configured microservice name between:

```text
/api/v1
```

and the resource path.

For example:

```java
@RequestMapping("/api/v1/questions")
```

with microservice:

```text
knowledge
```

becomes:

```text
/api/v1/knowledge/questions
```

---

# 8. Unauthenticated endpoints

If a controller uses:

```text
/rest/v1/...
```

instead of:

```text
/api/v1/...
```

consider the endpoint **unauthenticated**.

Append:

```text
(unauthenticated)
```

to the `Action`.

Example:

```text
Projects (unauthenticated)
```

For example:

```java
@RequestMapping("/rest/v1/projects")
```

becomes:

```text
GET <server address>/rest/v1/knowledge/projects
```

and its action should indicate:

```text
Projects (unauthenticated)
```

If the endpoint uses:

```text
/api/v1/...
```

append:

```text
(authenticated)
```

when appropriate.

Example:

```text
Projects (authenticated)
```

Do not attempt to infer authentication from unrelated security configuration. For this documentation task, use the `/rest/v1` versus `/api/v1` convention.

---

# 9. Action column

Infer a human-readable `Action` from:

1. controller name;
2. controller method name;
3. method parameters;
4. endpoint semantics.

Prefer concise hierarchical names.

Examples:

```text
Projects (unauthenticated)
Projects (authenticated)
Projects->View All
Projects->View Details by ID
Questions->Find All
Questions->Search by Pattern
Questions->Recently Added
Questions->Count Total
Questions->View Details by ID
Questions->Create Question
Questions->Update Question
Questions->Delete Question
```

Do not simply copy the Java method name if a more meaningful business-level action can be inferred.

---

# 10. GET endpoints

For `GET` endpoints:

* provide only the response example in the `Request Payload / Response` column;
* do not provide a request body.

Example:

```text
[{"id": 1, "name": "Knowledge Platform"}]
```

If the endpoint returns `ResponseEntity` but its actual body cannot be established reliably from the source code, use:

```text
returns ResponseEntity
```

Do not invent a response body.

---

# 11. POST and PUT endpoints

For `POST` and `PUT` endpoints, provide both request and response examples.

Use:

```text
Req: {...} · Resp: {...}
```

Determine the request structure from the actual `@RequestBody` DTO/class.

Determine the response structure from the actual return type and implementation.

For example:

```text
Req: {"question": "What is DI?", "shortAnswer": "A pattern for IoC."}
· Resp: {"id": 102, "question": "What is DI?", "shortAnswer": "A pattern for IoC."}
```

Only include fields supported by the actual source code.

---

# 12. DELETE endpoints

For every `DELETE` endpoint:

1. identify the path;
2. identify all `@PathVariable` parameters;
3. inspect the method return type;
4. inspect `ResponseEntity`, if present;
5. determine the actual HTTP status returned.

For example:

```java
@DeleteMapping(value = "/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    questionProcessor.delete(id);
    return new ResponseEntity<>(HttpStatusCode.valueOf(204));
}
```

should produce:

```text
DELETE <server address>/api/v1/knowledge/questions/101
```

and:

```text
Req: none · Resp: HTTP 204 No Content (empty body)
```

Do not assume that every DELETE returns `204`. Inspect the implementation.

---

# 13. @PathVariable

For every `@PathVariable`, document the parameter and provide an example value.

Example:

```java
@PathVariable Long id
```

should be represented as:

```text
id: number
```

and the route should contain an example:

```text
GET <server address>/api/v1/knowledge/questions/101
```

Use the actual Java type to determine the documentation type.

Examples:

```text
Long      -> number
Integer   -> number
int       -> number
String    -> string
UUID      -> UUID
Boolean   -> boolean
```

If multiple path variables exist, document all of them.

---

# 14. @RequestParam

For every `@RequestParam`, document:

* parameter name;
* Java type;
* default value, if present;
* an example complete URL.

For example:

```java
@RequestParam(defaultValue = "3") int limit
```

should result in:

```text
limit: number
```

and:

```text
GET <server address>/api/v1/knowledge/questions/recent?limit=3
```

If the endpoint contains:

```java
@RequestParam String pattern
```

use:

```text
pattern: string
```

and provide an example such as:

```text
GET <server address>/api/v1/knowledge/questions/search?pattern=Spring
```

If there are multiple query parameters, include all of them in the URL example.

---

# 15. Request body examples

When a controller accepts `@RequestBody`, inspect the corresponding DTO/class.

Build a representative JSON request based on the actual fields.

For nested DTOs, inspect the nested classes as well.

For example, if the source contains a structure equivalent to:

```text
QuestionRequest
  question
  shortAnswer
  detailedAnswer
  questionLevel
  codeExample
  tags
  resources
  projects
```

the generated request example should reflect that actual structure.

Do not create arbitrary fields merely to make the example look complete.

---

# 16. Response examples

Inspect the actual response type.

For example:

```java
ResponseEntity<List<QuestionDto>>
```

should result in an array example.

A response such as:

```java
ResponseEntity<QuestionDetails>
```

should result in an object example.

If a response is:

```java
ResponseEntity<Void>
```

document the HTTP status and indicate an empty response body where appropriate.

When the response is constructed by a service/processor, inspect that implementation if necessary to understand the actual returned structure.

---

# 17. One table per controller

Each generated Markdown file must contain a separate table for every controller.

Use the following table format exactly:

```markdown
## <ControllerName>

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| ... | ... | ... |
```

Do not combine multiple controllers into one table.

For example:

```markdown
## ProjectsController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Projects (unauthenticated) | GET <server address>/rest/v1/knowledge/projects/count | 5 |
| Projects (authenticated) | GET <server address>/api/v1/knowledge/projects/count | 3 |
| Projects->View All | GET <server address>/api/v1/knowledge/projects | [{"id": 1, "name": "Knowledge Platform"}] |
```

And:

```markdown
## QuestionsController

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| Questions (authenticated) | GET <server address>/api/v1/knowledge/questions | [...] |
| Questions->Search by Pattern | GET <server address>/api/v1/knowledge/questions/search?pattern=Spring | [...] |
| Questions->Recently Added | GET <server address>/api/v1/knowledge/questions/recent?limit=3 | [...] |
| Questions->View Details by ID | GET <server address>/api/v1/knowledge/questions/101 | {...} |
| Questions->Create Question | POST <server address>/api/v1/knowledge/questions | Req: {...} · Resp: {...} |
| Questions->Update Question | PUT <server address>/api/v1/knowledge/questions/101 | Req: {...} · Resp: {...} |
| Questions->Delete Question | DELETE <server address>/api/v1/knowledge/questions/101 | Req: none · Resp: HTTP 204 No Content (empty body) |
```

---

# 18. Suggested Markdown document structure

Each generated file should have the following structure:

```markdown
# <Microservice Name> API Contract

## Overview

- Repository: `<repository-name>`
- Microservice: `<microservice-name>`

## <ControllerName>

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| ... | ... | ... |

## <AnotherControllerName>

| Action | Route or REST API | Request Payload / Response |
|---|---|---|
| ... | ... | ... |
```

Keep the overview concise.

The main content must be the controller API tables.

---

# 19. Crawl strategy

Process repositories **one at a time**.

For each repository:

### Step 1 — Verify repository

Confirm that the repository is one of the included repositories.

### Step 2 — Determine microservice name

Use the explicit repository → microservice mapping above.

### Step 3 — Find controllers

Search the repository for:

```text
@RestController
```

### Step 4 — Inspect mappings

For every controller, inspect:

```text
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
```

### Step 5 — Inspect DTOs

For POST/PUT endpoints, inspect request DTOs.

For all endpoints, inspect response DTOs/classes where necessary.

### Step 6 — Inspect implementation

If the controller alone does not provide enough information to determine the response or HTTP status, inspect the referenced service/processor implementation.

Do not unnecessarily crawl unrelated code.

### Step 7 — Generate Markdown

Create/update:

```text
doc/<microservice-name>-api-contract.md
```

### Step 8 — Verify

After generating the file, verify that:

* every REST controller was included;
* every supported mapping method was included;
* every endpoint has an HTTP method;
* every route contains the correct microservice name;
* `/rest/v1` endpoints are marked unauthenticated;
* `/api/v1` endpoints are marked authenticated where applicable;
* path variables have example values;
* request parameters have example values;
* POST/PUT endpoints contain request and response examples;
* GET endpoints contain only response examples;
* DELETE endpoints contain the actual response status;
* there are no invented endpoints;
* there are no invented DTO fields;
* the file is located under the repository's `doc` directory.

---

# 20. Important: do not confuse repositories

Some repositories may contain dependencies or references to other microservices.

Document **only the REST controllers that physically belong to the repository currently being crawled**.

Do not document an endpoint merely because:

* another service calls it;
* it appears in configuration;
* it appears in a Feign/WebClient client;
* it appears in documentation;
* it appears in tests for another service.

The source controller must belong to the repository being documented.

---

# 21. Important: workers and agents

Some included repositories are named `worker` or `agent`.

Do not assume that they have no REST APIs.

Inspect them in exactly the same way as API repositories.

If a worker/agent contains `@RestController` classes and supported mapping annotations, document them.

If it contains no REST controllers, still create its Markdown contract file with a concise statement such as:

```markdown
# Import API Contract

## REST Controllers

No `@RestController` endpoints were found in this repository.
```

Do not invent endpoints.

---

# 22. Final verification across all repositories

After processing all repositories, verify that exactly these eight contract files exist:

```text
ti-gateway-api/doc/gateway-api-contract.md
ti-gateway-api/doc/knowledge-api-contract.md
ti-gateway-api/doc/orchestrator-api-contract.md
ti-gateway-api/doc/import-api-contract.md
ti-gateway-api/doc/export-api-contract.md
ti-gateway-api/doc/ai-orchestrator-api-contract.md
ti-gateway-api/doc/document-worker-api-contract.md
ti-gateway-api/doc/document-agent-api-contract.md
```

There must be:

```text
8 microservices
8 Markdown API-contract files
```

The following must **not** receive an API-contract file:

```text
ti-sql-agent
any directory ending with db
any directory ending with ui
```

---

# 23. Final response

When the work is complete, provide a concise summary containing:

1. repositories processed;
2. repositories skipped;
3. number of controllers found per repository;
4. number of REST endpoints documented per repository;
5. paths of generated Markdown files;
6. any endpoints or response structures that could not be determined reliably from source code.

Do not include large amounts of generated Markdown in the final response. The Markdown files themselves are the deliverables.

**Begin by inspecting `~/ti-2026`, identify the repositories according to the rules above, and then process each included repository.**
