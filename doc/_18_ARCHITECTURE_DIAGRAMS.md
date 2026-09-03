There are also two important distinctions:

* `ti-knowledge-ui` is the **entry point for the whole TI Platform**.
* `ti-ai-chatbot-ui` and `ti-ai-question-ui` are separate frontend applications reached through the platform/gateway.
* `ti-orchestrator-api` is for the **import/export workflows**, while `ti-ai-orchestrator-api` is specifically responsible for the **AI chatbot / AI orchestration**.
* `ti-document-worker` handles document ingestion/ETL and stores both **embeddings in `ti-document-db`** and **document text sections** for question generation.
* `ti-document-agent` and `ti-sql-question-agent` are the AI agents.

## Current architecture 

```mermaid
flowchart TB

    %% =========================================================
    %% Frontend
    %% =========================================================

    subgraph UI["Frontend Applications"]
        direction LR

        KUI["ti-knowledge-ui<br/><br/>TI Platform Entry Point<br/>Dashboard / Base Information / Login"]

        CUI["ti-ai-chatbot-ui<br/><br/>AI Chatbot<br/>Chat / RAG"]

        QUI["ti-ai-question-ui<br/><br/>AI Resources Uploader<br/>Question Generation"]
    end


    %% =========================================================
    %% Gateway
    %% =========================================================

    GW["ti-gateway-api<br/><br/>API Gateway / BFF<br/>OAuth2 Client / Security / Routing"]


    %% =========================================================
    %% Core APIs
    %% =========================================================

    subgraph API["Backend APIs"]
        direction LR

        KA["ti-knowledge-api<br/><br/>Questions / Answers<br/>Resources / Code Examples"]

        OA["ti-orchestrator-api<br/><br/>Long-running<br/>Import / Export workflows"]

        AIO["ti-ai-orchestrator-api<br/><br/>AI Chatbot Orchestration"]
    end


    %% =========================================================
    %% Workers
    %% =========================================================

    subgraph WORKERS["Workers / Processing"]
        direction LR

        IW["ti-import-worker<br/><br/>Excel / CSV Import"]

        DW["ti-document-worker<br/><br/>Document ETL / Processing<br/>Embeddings + Text Sections"]

        EA["ti-export-api<br/><br/>Export File Generation"]
    end


    %% =========================================================
    %% AI Agents
    %% =========================================================

    subgraph AGENTS["AI Agents"]
        direction LR

        DA["ti-document-agent<br/><br/>Document AI Agent<br/>RAG / Vector Search"]

        SA["ti-sql-question-agent<br/><br/>Question AI Agent<br/>SQL Generation / Knowledge DB"]
    end


    %% =========================================================
    %% Databases
    %% =========================================================

    subgraph DB["Databases"]
        direction LR

        KDB[("ti-knowledge-db<br/><br/>Questions<br/>Answers<br/>Tags<br/>Question Levels<br/>Resources<br/>Code Examples")]

        DDB[("ti-document-db<br/><br/>Document Embeddings<br/>PGVector<br/>Document Text Sections")]

        ADB[("ti-assistant-db<br/><br/>User Messages<br/>AI Processing Results<br/>Conversations")]
    end


    %% =========================================================
    %% Platform Entry Point
    %% =========================================================

    KUI --> GW

    %% AI UIs
    CUI --> GW
    QUI --> GW


    %% =========================================================
    %% Gateway routing
    %% =========================================================

    GW --> KA
    GW --> OA
    GW --> AIO


    %% =========================================================
    %% Knowledge API
    %% =========================================================

    KA --> KDB


    %% =========================================================
    %% Import / Export workflows
    %% =========================================================

    OA --> IW
    OA --> EA

    IW --> KDB


    %% =========================================================
    %% Document upload / processing
    %% =========================================================

    QUI -->|"Upload document"| GW
    GW -->|"Document processing"| DW

    DW --> DDB
    DW -->|"Generated / extracted document text"| DDB


    %% =========================================================
    %% AI Chatbot
    %% =========================================================

    AIO --> DA
    AIO --> SA
    AIO --> ADB


    %% =========================================================
    %% Document Agent
    %% =========================================================

    DA --> DDB


    %% =========================================================
    %% SQL Question Agent
    %% =========================================================

    SA --> KDB
```

### One thing I would change in the above diagram

The direct:

```text
ti-ai-question-ui
        ↓
ti-gateway-api
        ↓
ti-document-worker
```

is conceptually correct, but your actual upload flow is more likely:

```text
ti-ai-question-ui
       │
       ▼
ti-gateway-api
       │
       ▼
ti-orchestrator-api
       │
       ▼
RabbitMQ
       │
       ▼
ti-document-worker
```

**if the document processing is implemented as a long-running asynchronous workflow.**

Given your current architecture, I would therefore recommend the following more accurate **logical architecture diagram**.

---

# Recommended architecture diagram

```mermaid
flowchart TB

    %% =========================================================
    %% USERS / FRONTEND
    %% =========================================================

    USER["User"]

    subgraph FRONTEND["Frontend Applications"]
        KUI["ti-knowledge-ui<br/><br/>TI Platform Entry Point<br/>Dashboard / Login"]

        CUI["ti-ai-chatbot-ui<br/><br/>AI Chatbot UI<br/>RAG Chat"]

        QUI["ti-ai-question-ui<br/><br/>AI Resources Uploader<br/>AI Question Generation"]
    end


    %% =========================================================
    %% API GATEWAY
    %% =========================================================

    GW["ti-gateway-api<br/><br/>API Gateway<br/>BFF<br/>OAuth2 Client<br/>Security<br/>Routing"]


    %% =========================================================
    %% BUSINESS APIs
    %% =========================================================

    subgraph APIS["Backend APIs"]
        KA["ti-knowledge-api<br/><br/>Knowledge Management"]

        ORCH["ti-orchestrator-api<br/><br/>Import / Export Workflow"]

        AIORCH["ti-ai-orchestrator-api<br/><br/>AI Chatbot Orchestration"]
    end


    %% =========================================================
    %% MESSAGE / ASYNC PROCESSING
    %% =========================================================

    MQ[("RabbitMQ")]

    subgraph PROCESSING["Processing Services"]
        IW["ti-import-worker<br/><br/>Excel / CSV Processing"]

        DW["ti-document-worker<br/><br/>Document ETL<br/>Chunking<br/>Embedding Generation<br/>Text Extraction"]
        
        EA["ti-export-api<br/><br/>Export Generation"]
    end


    %% =========================================================
    %% AI AGENTS
    %% =========================================================

    subgraph AI["AI Agents"]
        DA["ti-document-agent<br/><br/>Document RAG Agent"]

        SA["ti-sql-question-agent<br/><br/>SQL Question Agent"]
    end


    %% =========================================================
    %% DATABASES
    %% =========================================================

    subgraph DATABASES["Databases"]
        KDB[("ti-knowledge-db<br/><br/>Questions<br/>Answers<br/>Tags<br/>Question Levels<br/>Resources<br/>Code Examples")]

        DDB[("ti-document-db<br/><br/>PGVector<br/>Document Embeddings<br/>Document Text Sections")]

        ADB[("ti-assistant-db<br/><br/>Conversations<br/>User Messages<br/>AI Results")]
    end


    %% =========================================================
    %% USER -> FRONTEND
    %% =========================================================

    USER --> KUI
    USER --> CUI
    USER --> QUI


    %% =========================================================
    %% FRONTEND -> GATEWAY
    %% =========================================================

    KUI --> GW
    CUI --> GW
    QUI --> GW


    %% =========================================================
    %% GATEWAY -> APIs
    %% =========================================================

    GW --> KA
    GW --> ORCH
    GW --> AIORCH


    %% =========================================================
    %% KNOWLEDGE
    %% =========================================================

    KA --> KDB


    %% =========================================================
    %% IMPORT / EXPORT
    %% =========================================================

    ORCH --> MQ

    MQ --> IW
    MQ --> DW

    ORCH --> EA

    IW --> KDB


    %% =========================================================
    %% DOCUMENT INGESTION
    %% =========================================================

    DW --> DDB


    %% =========================================================
    %% AI CHATBOT
    %% =========================================================

    AIORCH --> DA
    AIORCH --> SA
    AIORCH --> ADB


    %% =========================================================
    %% DOCUMENT RAG
    %% =========================================================

    DA --> DDB


    %% =========================================================
    %% SQL QUESTION GENERATION
    %% =========================================================

    SA --> KDB
```

---

# 2. The two most important flows

These two flows should be very clear in architecture documentation.

## A. AI Resources Uploader + Question Generation

```mermaid
flowchart LR

    UI["ti-ai-question-ui<br/><br/>Upload Document"]

    GW["ti-gateway-api"]

    ORCH["ti-orchestrator-api"]

    MQ[("RabbitMQ")]

    DW["ti-document-worker<br/><br/>Document ETL"]

    DDB[("ti-document-db<br/><br/>PGVector + Text Sections")]

    UI --> GW
    GW --> ORCH
    ORCH --> MQ
    MQ --> DW
    DW --> DDB

    DDB -->|"Document text / resources"| DW

    DW -->|"Ready for question generation"| ORCH
    ORCH --> UI
```

Then question generation:

```mermaid
flowchart LR

    UI["ti-ai-question-ui<br/><br/>Generate Questions"]

    GW["ti-gateway-api"]

    AIORCH["ti-ai-orchestrator-api"]

    DA["ti-document-agent"]

    DDB[("ti-document-db<br/><br/>Document Text Sections")]

    LLM["LLM"]

    UI --> GW
    GW --> AIORCH
    AIORCH --> DA
    DA --> DDB
    DA --> LLM

    LLM --> DA
    DA --> AIORCH
    AIORCH --> UI

    UI -->|"Display"| TABLE["Interview Questions Table"]
```

---

# 3. AI Chatbot flow

And the second application has a completely different responsibility:

```mermaid
flowchart LR

    USER["User"]

    UI["ti-ai-chatbot-ui<br/><br/>Chat"]

    GW["ti-gateway-api"]

    AIO["ti-ai-orchestrator-api"]

    DA["ti-document-agent<br/><br/>RAG"]

    SA["ti-sql-question-agent<br/><br/>SQL"]

    DDB[("ti-document-db<br/><br/>PGVector")]

    KDB[("ti-knowledge-db")]

    ADB[("ti-assistant-db")]

    USER --> UI
    UI --> GW
    GW --> AIO

    AIO --> DA
    AIO --> SA

    DA --> DDB
    SA --> KDB

    AIO --> ADB

    DA --> AIO
    SA --> AIO

    AIO -->|"SSE"| GW
    GW --> UI
```

This makes the distinction between the two applications very clear:

```text
ti-ai-question-ui
        │
        ├── Upload resources
        ├── Generate interview questions
        ├── Review questions
        └── Save questions
```

versus:

```text
ti-ai-chatbot-ui
        │
        ├── Ask questions
        ├── Classical RAG
        ├── SQL questions
        └── Receive AI answers
```

---

# 4. Complete current microservice map

Based on current implementation, the services as:

```mermaid
flowchart TB

    subgraph UI["UI Applications"]
        KUI["ti-knowledge-ui"]
        CUI["ti-ai-chatbot-ui"]
        QUI["ti-ai-question-ui"]
    end

    subgraph PLATFORM["Platform APIs"]
        GW["ti-gateway-api"]
        KA["ti-knowledge-api"]
        ORCH["ti-orchestrator-api"]
        AIO["ti-ai-orchestrator-api"]
    end

    subgraph WORKERS["Workers / Processing"]
        IW["ti-import-worker"]
        DW["ti-document-worker"]
        EA["ti-export-api"]
    end

    subgraph AGENTS["AI Agents"]
        DA["ti-document-agent"]
        SA["ti-sql-question-agent"]
    end

    subgraph DATA["Databases"]
        KDB[("ti-knowledge-db")]
        DDB[("ti-document-db")]
        ADB[("ti-assistant-db")]
    end

    MQ[("RabbitMQ")]


    KUI --> GW
    CUI --> GW
    QUI --> GW

    GW --> KA
    GW --> ORCH
    GW --> AIO

    KA --> KDB

    ORCH --> MQ

    MQ --> IW
    MQ --> DW

    IW --> KDB
    DW --> DDB

    ORCH --> EA

    AIO --> DA
    AIO --> SA

    DA --> DDB
    SA --> KDB

    AIO --> ADB
```

Absolutely. If both AI Agents need access to the assistant-side data, we should connect **`ti-document-agent`** and **`ti-sql-question-agent`** to **`ti-assistant-db`**.

Updated **#4 Complete current microservice map**:

```mermaid
flowchart TB

    subgraph UI["UI Applications"]
        KUI["ti-knowledge-ui"]
        CUI["ti-ai-chatbot-ui"]
        QUI["ti-ai-question-ui"]
    end

    subgraph PLATFORM["Platform APIs"]
        GW["ti-gateway-api"]
        KA["ti-knowledge-api"]
        ORCH["ti-orchestrator-api"]
        AIO["ti-ai-orchestrator-api"]
    end

    subgraph WORKERS["Workers / Processing"]
        IW["ti-import-worker"]
        DW["ti-document-worker"]
        EA["ti-export-api"]
    end

    subgraph AGENTS["AI Agents"]
        DA["ti-document-agent"]
        SA["ti-sql-question-agent"]
    end

    subgraph DATA["Databases"]
        KDB[("ti-knowledge-db")]
        DDB[("ti-document-db")]
        ADB[("ti-assistant-db")]
    end

    MQ[("RabbitMQ")]

    %% =========================
    %% UI -> Gateway
    %% =========================

    KUI --> GW
    CUI --> GW
    QUI --> GW

    %% =========================
    %% Gateway -> APIs
    %% =========================

    GW --> KA
    GW --> ORCH
    GW --> AIO

    %% =========================
    %% Knowledge Platform
    %% =========================

    KA --> KDB

    %% =========================
    %% Import / Document Processing
    %% =========================

    ORCH --> MQ

    MQ --> IW
    MQ --> DW

    IW --> KDB
    DW --> DDB

    ORCH --> EA

    %% =========================
    %% AI Orchestration
    %% =========================

    AIO --> DA
    AIO --> SA

    %% =========================
    %% AI Agents -> Data
    %% =========================

    DA --> DDB
    DA --> ADB

    SA --> KDB
    SA --> ADB

    %% =========================
    %% AI Orchestrator -> Assistant DB
    %% =========================

    AIO --> ADB
```

### The important change

Previously:

```text
ti-document-agent  --> ti-document-db
ti-sql-question-agent --> ti-knowledge-db
```

Now:

```text
ti-document-agent  --> ti-document-db
                   --> ti-assistant-db

ti-sql-question-agent --> ti-knowledge-db
                      --> ti-assistant-db
```

So the responsibilities become:

| Component                | Primary DB        | Assistant DB |
| ------------------------ | ----------------- | ------------ |
| `ti-document-agent`      | `ti-document-db`  | ✅            |
| `ti-sql-question-agent`  | `ti-knowledge-db` | ✅            |
| `ti-ai-orchestrator-api` | —                 | ✅            |
| `ti-knowledge-api`       | `ti-knowledge-db` | —            |
| `ti-document-worker`     | `ti-document-db`  | —            |
| `ti-import-worker`       | `ti-knowledge-db` | —            |

This makes sense if **`ti-assistant-db` contains conversation/message/AI-processing context that the agents themselves need to read or update**, rather than making the orchestrator the only component allowed to access it.

---

Yes. The best approach is to make the **second diagram self-contained**, while preserving the richer descriptions from the first diagram and adding **RabbitMQ** plus the updated **AI Agent → `ti-assistant-db`** connections.

Here is the combined version:

```mermaid
flowchart TB

    %% =========================================================
    %% Frontend Applications
    %% =========================================================

    subgraph UI["Frontend Applications"]
        direction LR

        KUI["ti-knowledge-ui<br/><br/>TI Platform Entry Point<br/>Dashboard / Base Information / Login"]

        CUI["ti-ai-chatbot-ui<br/><br/>AI Chatbot<br/>Chat / RAG"]

        QUI["ti-ai-question-ui<br/><br/>AI Resources Uploader<br/>Question Generation"]
    end


    %% =========================================================
    %% API Gateway
    %% =========================================================

    GW["ti-gateway-api<br/><br/>API Gateway / BFF<br/>OAuth2 Client / Security / Routing"]


    %% =========================================================
    %% Backend APIs
    %% =========================================================

    subgraph API["Backend APIs"]
        direction LR

        KA["ti-knowledge-api<br/><br/>Questions / Answers<br/>Resources / Code Examples"]

        ORCH["ti-orchestrator-api<br/><br/>Long-running<br/>Import / Export Workflows"]

        AIO["ti-ai-orchestrator-api<br/><br/>AI Chatbot Orchestration"]
    end


    %% =========================================================
    %% Workers / Processing
    %% =========================================================

    subgraph WORKERS["Workers / Processing"]
        direction LR

        IW["ti-import-worker<br/><br/>Excel / CSV Import"]

        DW["ti-document-worker<br/><br/>Document ETL / Processing<br/>Embeddings + Text Sections"]

        EA["ti-export-api<br/><br/>Export File Generation"]
    end


    %% =========================================================
    %% AI Agents
    %% =========================================================

    subgraph AGENTS["AI Agents"]
        direction LR

        DA["ti-document-agent<br/><br/>Document AI Agent<br/>RAG / Vector Search"]

        SA["ti-sql-question-agent<br/><br/>Question AI Agent<br/>SQL Generation / Knowledge DB"]
    end


    %% =========================================================
    %% Messaging
    %% =========================================================

    MQ[("RabbitMQ<br/><br/>Async Messaging")]


    %% =========================================================
    %% Databases
    %% =========================================================

    subgraph DB["Databases"]
        direction LR

        KDB[("ti-knowledge-db<br/><br/>Questions<br/>Answers<br/>Tags<br/>Question Levels<br/>Resources<br/>Code Examples")]

        DDB[("ti-document-db<br/><br/>Document Embeddings<br/>PGVector<br/>Document Text Sections")]

        ADB[("ti-assistant-db<br/><br/>User Messages<br/>Conversations<br/>AI Processing Results")]
    end


    %% =========================================================
    %% Frontend -> Gateway
    %% =========================================================

    KUI --> GW
    CUI --> GW
    QUI --> GW


    %% =========================================================
    %% Gateway -> Backend APIs
    %% =========================================================

    GW --> KA
    GW --> ORCH
    GW --> AIO


    %% =========================================================
    %% Knowledge API
    %% =========================================================

    KA --> KDB


    %% =========================================================
    %% Import / Export Workflows
    %% =========================================================

    ORCH -->|"Import / Export commands"| MQ

    MQ -->|"Import messages"| IW
    MQ -->|"Document processing messages"| DW

    IW -->|"Imported questions / data"| KDB

    ORCH --> EA


    %% =========================================================
    %% Document Upload / Processing
    %% =========================================================

    QUI -->|"Upload document"| GW
    GW -->|"Start document processing"| ORCH

    DW -->|"Document embeddings / vectors"| DDB
    DW -->|"Document text sections"| DDB


    %% =========================================================
    %% AI Chatbot Orchestration
    %% =========================================================

    AIO -->|"Document query"| DA
    AIO -->|"Question / SQL query"| SA

    AIO -->|"Conversations / messages / AI results"| ADB


    %% =========================================================
    %% Document AI Agent
    %% =========================================================

    DA -->|"Vector search / document retrieval"| DDB
    DA -->|"Conversation / processing context"| ADB


    %% =========================================================
    %% SQL Question AI Agent
    %% =========================================================

    SA -->|"SQL / question data"| KDB
    SA -->|"Conversation / processing context"| ADB
```

### prefer this version

It combines the **visual/business descriptions** of the first diagram 
with the **actual technical communication paths** of the second one:

```text
                         FRONTENDS
     ┌──────────────────────────────────────────────┐
     │ knowledge-ui   chatbot-ui   question-ui     │
     └──────────────────────┬───────────────────────┘
                            │
                            ▼
                    ti-gateway-api
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
       knowledge-api   orchestrator-api   ai-orchestrator
             │              │              │
             ▼              ▼              ▼
      knowledge-db     RabbitMQ          AI Agents
                            │          ┌────┴────┐
                       ┌────┴────┐     ▼         ▼
                       │         │ document   SQL Question
                       ▼         ▼   Agent       Agent
                  import-worker document-worker │
                       │         │               │
                       ▼         ▼               ▼
                  knowledge-db document-db   knowledge-db

                         AI Agents
                            │
                            ▼
                     assistant-db
```

One important correction from the first diagram is also incorporated:

**`ti-ai-question-ui` does not directly call `ti-document-worker`.**

Instead:

```text
ti-ai-question-ui
       │
       ▼
ti-gateway-api
       │
       ▼
ti-orchestrator-api
       │
       ▼
   RabbitMQ
       │
       ▼
ti-document-worker
```

This keeps the UI independent of the worker and preserves the asynchronous workflow.

And for the AI side:

```text
ti-ai-orchestrator-api
        │
        ├──────────────► ti-document-agent ─────► ti-document-db
        │                         │
        │                         └─────────────► ti-assistant-db
        │
        └──────────────► ti-sql-question-agent ─► ti-knowledge-db
                                  │
                                  └─────────────► ti-assistant-db
```

So this single Mermaid diagram can now be used **independently as the complete current architecture diagram**.
