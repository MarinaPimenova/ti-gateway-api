Please generate complete instruction for local setup and run in docker compose the TI Knowledge platform.

Take into account the following:

- docker/docker-compose-full.yml - if this configuration contains contradictions please fix them.
- docker/env.example - align this file according to the environment variables that are mentioned in docker/docker-compose-full.yml.
- doc/_06_LOCAL_SETUP.md - As a result, this file should be refactored.

You should be specific and concise.

---

Please update:
- docker/docker-compose-full.yml and docker/env.example according to `src/main/resources/application.yml`. 

- Please be careful with section `ms.cors.allowed-origins, ms.client-to-url` in `src/main/resources/application.yml`:

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

So, maybe it makes sense to return back for `ti-gateway-api` the previous service name - `localhost` ?