Please refactor `README.md` by adding the last section about "how to run this microservice 
as standalone service" and taking into account the following:

- `src/main/resources/application.yaml`
- and to have possibility to run it locally as one standalone microservice to check functionality
so, please provide set http files for proxying different microservices that are mentioned in the application.ym:
```yaml
  ms:
  cors:
    allowed-origins: http://localhost:5000,http://localhost:7000,http://localhost:5173,http://localhost:4000
  origin: ${APPLICATION_URL:http://localhost:8080}
  spa:
    dashboard-base-url: "${KNOWLEDGE_BASE_URL:http://localhost:5000}"
    ai-chatbot-base-url: "${AI_CHATBOT_BASE_URL:http://localhost:7000}"
    ai-question-base-url: "${AI_QUESTION_BASE_URL:http://localhost:4000}"
  client-to-url:
    knowledge-url: "${KNOWLEDGE_BASE_URL:http://localhost:5000}/dashboard-page"
    ai-chatbot-url: "${AI_CHATBOT_BASE_URL:http://localhost:7000}/chat"
    ai-question-url: "${AI_QUESTION_BASE_URL:http://localhost:4000}/document"
  service-name-to-uri:
    knowledge: http://${KNOWLEDGE_SERVICE:localhost}:${KNOWLEDGE_SERVICE_PORT:8081}
    orchestrator: http://${ORCHESTRATOR_SERVICE:localhost}:${ORCHESTRATOR_SERVICE_PORT:8082}
    import: http://${IMPORT_SERVICE:localhost}:${IMPORT_SERVICE_PORT:8083}
    export: http://${EXPORT_SERVICE:localhost}:${EXPORT_SERVICE_PORT:8084}
    ai-orchestrator: http://${AI_ORCHESTRATOR_SERVICE:localhost}:${AI_ORCHESTRATOR_SERVICE_PORT:8085}
    document: http://${DOCUMENT_SERVICE:localhost}:${DOCUMENT_SERVICE_PORT:8086}
    document-agent: http://${DOCUMENT_AGENT:localhost}:${DOCUMENT_AGENT_PORT:8087}
    sql-agent: http://${SQL_AGENT:localhost}:${SQL_AGENT_PORT:8088}
```

Please crawl all repositories in `/Users/Marina_Pimenova/ti-2026/` to search all exposed API in classes 
that are marked `@RestController` except:
- ending on `db`,
- ending on `ui`.
- (ti-sql-agent repository should be skipped)

I need to have the complete list of APIs from all BE microservices in the following table format:

- URL to api in the format 
```text
POST http://localhost:8080/api/v1/<Service name according to `ms.service-name-to-uri`>/questions
Content-Type: application/json
Accept: application/json
```
for example,
```text
POST http://localhost:8080/api/v1/knowledge/questions
Content-Type: application/json
Accept: application/json
```
- payload - if exists. For example,
```text

{
  "question": "<QUESTION>",
  "shortAnswer": "<ANSWER>",
  "questionLevelId": 1,
  "tagIds": [17],
  "projectIds": [],
  "resources": []
}
```
- response.

As a result, we will have:
- the refactored `README.md` file:
1) structured existing sections.
2) + "how to run locally and test functionality" - it should be complete guide like:
-what standalone docker compose should be run - see the list in `/Users/Marina_Pimenova/ti-2026/ti-gateway-api/docker`
-set of http files under `/Users/Marina_Pimenova/ti-2026/ti-gateway-api/http` folder to cover the main scenarios of this microservice.

During output generation be concise.