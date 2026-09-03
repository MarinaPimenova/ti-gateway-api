# **two separate applications**
 
[`ti-chatbot-ui`](https://github.com/MarinaPimenova/ti-chatbot-ui) repository: it is already a React + TypeScript + Vite application, so I would keep that repository focused exclusively on the **end-user conversational experience**. ([GitHub][1])

Vite officially supports the `react-ts` template, so the second application should also start from `react-ts`. ([vitejs][2])

# 1. Two UI applications


```text
                    TI AI Platform
                         │
             ┌───────────┴───────────┐
             │                       │
             ▼                       ▼
   ┌────────────────────┐  ┌────────────────────────┐
   │   ti-chatbot-ui    │  │ ti-ai-question-ui      │
   │                    │  │                        │
   │ End-user chatbot   │  │ Admin/content UI       │
   │                    │  │                        │
   │ • Chat             │  │ • Upload documents     │
   │ • RAG questions    │  │ • View uploaded docs   │
   │ • Answers          │  │ • Generate questions   │
   │ • Conversation     │  │ • Review questions     │
   │                    │  │ • Save to Knowledge    │
   └─────────┬──────────┘  └────────────┬───────────┘
             │                          │
             └────────────┬─────────────┘
                          ▼
                 ti-orchestrator-api
                          │
             ┌────────────┼────────────┐
             ▼            ▼            ▼
        Document AI    SQL AI       Hello AI
             │
             ▼
          PGVector
```

## Name for the second UI

The name is:

### `ti-ai-question-ui`

It is short, consistent with `ti-chatbot-ui`, and describes its main responsibility.

Alternative:

* `ti-ai-content-ui`
* `ti-ai-admin-ui`
* `ti-ai-question-generator-ui`
* `ti-knowledge-ai-ui`

---

# 2. Responsibility of each UI

## `ti-chatbot-ui`

This is the application you already have:

```text
┌─────────────────────────────────────────┐
│ TI Knowledge Platform             User  │
├─────────────────────────────────────────┤
│                                         │
│ Chat                                    │
│                                         │
│ AI: Hello!                              │
│                                         │
│ User: What is dependency injection?     │
│                                         │
│ AI: Dependency injection is...          │
│                                         │
│                                         │
│ ┌─────────────────────────────┐ [Send] │
│ │ Ask your question...        │         │
│ └─────────────────────────────┘         │
│                                         │
├─────────────────────────────────────────┤
│ Footer                                  │
└─────────────────────────────────────────┘
```

It should **not upload documents**.

It should **not generate the administrative question set**.

It simply talks to the knowledge base.

---

# 3. `ti-ai-question-ui`

This application becomes the administration/content-generation UI:

```text
┌───────────────────────────────────────────────────────┐
│ TI Knowledge Platform                   User           │
├───────────────────────────────────────────────────────┤
│                 AI Question Generator                 │
│                                                       │
│ Upload documents:                                     │
│ • to generate interview questions                     │
│ • to provide resources for classical RAG              │
│             ┌────────────────────────┐                │
│             │   📎 UPLOAD DOCUMENT   │                │
│             └────────────────────────┘                │
├───────────────────────────────────────────────────────┤
│ Please select the Uploaded Resource for which the questions
│  will be generated :                                  │
│ • java.pdf                                            │
│ • spring-boot.pdf                                     │
│ • aws.pdf                                             │
│ Please select the number of generated question: 10, 20,30
│ Please provide your shor prompt for questions generation:
(Optional can be left empty)
│ ┌─────────────────────────────┐                       │
│ │  your prompt / comment...   │                       │     
│ └─────────────────────────────┘                       │
│             ┌────────────────────────┐                │
│             │   GENERATE QUESTIONS   │                │
│             └────────────────────────┘                │
├───────────────────────────────────────────────────────┤
│ while questions are being generated 
 here is should be default view such as:
 "questions generation is in progress ..."

Generated Interview Questions                           │
│                                                       │
│ ┌────┬──────────────────┬──────────────┬────────────┐ │
│ │ #  │ Interview        │ Short Answer │ Tag        │ │
│ ├────┼──────────────────┼──────────────┼────────────┤ │
│ │ 1  │ What is ...      │ ...          │ Java       │ │
│ │ 2  │ Explain ...      │ ...          │ Spring     │ │
│ └────┴──────────────────┴──────────────┴────────────┘ │
│                                                       │
│                  < 1 2 3 >                            │
├───────────────────────────────────────────────────────┤
│        Save questions in TI Knowledge Platform?       │
│                                                       │
│   [ SAVE ] (This functionality is under construction) │
├───────────────────────────────────────────────────────┤
│ Footer                                                │
└───────────────────────────────────────────────────────┘
```

I would actually add the **Uploaded Resources** section. It gives the user immediate confirmation that ingestion succeeded and also gives us a natural place to display the `metadata->filename` information.

---

# 4. Create the second application

Use:

```bash
npm create vite@latest ti-ai-question-ui -- --template react-ts
```

Then:

```bash
cd ti-ai-question-ui

npm install

npm install antd axios

npm install -D sass
```

The official Vite template supports `react-ts`. ([vitejs][2])

I would also explicitly type-check the application in CI/build because Vite transpiles TypeScript but does not itself perform full type checking. ([GitHub][3])

For example:

```bash
npm run build
```

with the build script configured to include:

```bash
tsc -b && vite build
```

---

# 5. Recommended project structure

For `ti-ai-question-ui` I recommend:

```text
ti-ai-question-ui/
│
├── public/
│
├── src/
│   │
│   ├── api/
│   │   ├── api.ts
│   │   ├── document.api.ts
│   │   ├── question.api.ts
│   │   └── knowledge.api.ts
│   │
│   ├── components/
│   │   │
│   │   ├── layout/
│   │   │   ├── Header/
│   │   │   │   ├── Header.tsx
│   │   │   │   └── Header.scss
│   │   │   │
│   │   │   └── Footer/
│   │   │       ├── Footer.tsx
│   │   │       └── Footer.scss
│   │   │
│   │   ├── upload/
│   │   │   ├── DocumentUpload.tsx
│   │   │   └── DocumentUpload.scss
│   │   │
│   │   ├── documents/
│   │   │   ├── DocumentList.tsx
│   │   │   └── DocumentList.scss
│   │   │
│   │   ├── questions/
│   │   │   ├── QuestionGenerator.tsx
│   │   │   ├── QuestionsTable.tsx
│   │   │   └── Questions.scss
│   │   │
│   │   └── common/
│   │       └── SectionDivider.tsx
│   │
│   ├── hooks/
│   │   ├── useDocuments.ts
│   │   └── useQuestions.ts
│   │
│   ├── types/
│   │   ├── document.ts
│   │   ├── question.ts
│   │   └── api.ts
│   │
│   ├── App.tsx
│   ├── App.scss
│   ├── index.scss
│   └── main.tsx
│
├── .env.development
├── .env.production
├── package.json
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
└── vite.config.ts
```

This is deliberately a **feature-oriented structure**, rather than putting everything into `App.tsx`.

---

# 6. API architecture

The second UI should have three API responsibilities.

```text
ti-ai-question-ui
       │
       ├── Document API
       │      ├── upload
       │      └── list
       │
       ├── Question API
       │      └── generate
       │
       └── Knowledge API
              └── save
```

For example:

```text
POST /rest/v1/import

GET  /rest/v1/documents

POST /rest/v1/questions/generate

POST /rest/v1/questions
```

The exact endpoint names can be adjusted to your current BE contracts.

---

# 7. Environment configuration

`.env.development`

```properties
VITE_API_BASE_URL=http://localhost:8085
```

`.env.production`

```properties
VITE_API_BASE_URL=/api
```

Then:

## `src/api/api.ts`

```typescript
import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    Accept: 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API request failed:', error);

    return Promise.reject(error);
  },
);
```

---

# 8. Types

## `src/types/question.ts`

```typescript
export interface InterviewQuestion {
  id?: number;
  question: string;
  shortAnswer: string;
  tag: string;
}

export interface GeneratedQuestionsResponse {
  type: 'interview-question';
  items: InterviewQuestion[];
}
```

---

## `src/types/document.ts`

```typescript
export interface DocumentsResponse {
  type: 'filename';
  items: string[];
}
```

I recommend keeping the response contract simple.

Backend:

```json
{
  "type": "filename",
  "items": [
    "java.pdf",
    "spring.pdf",
    "aws.pdf"
  ]
}
```

Frontend:

```typescript
string[]
```

---

## `src/types/api.ts`

```typescript
export interface ApiErrorResponse {
  status: number;
  message: string;
  timestamp?: string;
}
```

---

# 9. Document API

## `src/api/document.api.ts`

```typescript
import { api } from './api';

import type { DocumentsResponse } from '../types/document';

export async function uploadDocument(
  file: File,
): Promise<void> {
  const formData = new FormData();

  formData.append('file', file);

  await api.post(
    '/rest/v1/import',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  );
}

export async function getDocuments(): Promise<string[]> {
  const response =
    await api.get<DocumentsResponse>(
      '/rest/v1/documents',
    );

  return response.data.items;
}
```

---

# 10. Document upload

I would use a large central upload button as you requested.

## `DocumentUpload.tsx`

```tsx
import {
  useRef,
  useState,
} from 'react';

import {
  Button,
  message,
  Typography,
} from 'antd';

import {
  PaperClipOutlined,
} from '@ant-design/icons';

import { uploadDocument } from '../../api/document.api';

import './DocumentUpload.scss';

const { Text } = Typography;

interface DocumentUploadProps {
  onUploaded?: () => void;
}

export function DocumentUpload({
  onUploaded,
}: DocumentUploadProps) {
  const inputRef =
    useRef<HTMLInputElement>(null);

  const [file, setFile] =
    useState<File | null>(null);

  const [uploading, setUploading] =
    useState(false);

  const handleSelect = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const selected =
      event.target.files?.[0] ?? null;

    setFile(selected);
  };

  const handleUpload = async () => {
    if (!file) {
      message.warning(
        'Please select a document first.',
      );

      return;
    }

    try {
      setUploading(true);

      await uploadDocument(file);

      message.success(
        `${file.name} uploaded successfully.`,
      );

      setFile(null);

      if (inputRef.current) {
        inputRef.current.value = '';
      }

      onUploaded?.();
    } catch {
      message.error(
        'Failed to upload the document.',
      );
    } finally {
      setUploading(false);
    }
  };

  return (
    <section className="document-upload">
      <div className="document-upload__text">
        <Text>
          Upload documents:
        </Text>

        <ul>
          <li>
            to generate interview questions;
          </li>

          <li>
            to provide answers to your questions
            based on the uploaded resources
            (classical RAG).
          </li>
        </ul>
      </div>

      <div className="document-upload__button-container">
        <input
          ref={inputRef}
          type="file"
          hidden
          onChange={handleSelect}
        />

        <Button
          type="primary"
          size="large"
          icon={<PaperClipOutlined />}
          className="document-upload__button"
          onClick={() =>
            inputRef.current?.click()
          }
        >
          UPLOAD DOCUMENT
        </Button>
      </div>

      {file && (
        <div className="document-upload__selected">
          Selected file:

          <strong>
            {file.name}
          </strong>

          <Button
            type="primary"
            loading={uploading}
            onClick={handleUpload}
          >
            Upload
          </Button>
        </div>
      )}
    </section>
  );
}
```

---

# 11. Upload CSS

```scss
.document-upload {
  padding: 2rem 0;

  &__text {
    font-size: 1rem;
    line-height: 1.6;

    ul {
      margin: 0.75rem 0 0;
      padding-left: 1.5rem;
    }
  }

  &__button-container {
    display: flex;
    justify-content: center;

    margin: 2.5rem 0 1.5rem;
  }

  &__button {
    min-width: 320px;
    height: 60px;

    font-size: 1.05rem;
    font-weight: 600;
  }

  &__selected {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;

    padding: 1rem;

    border-radius: 6px;

    background: #f5f7fa;
  }
}
```

---

# 12. Uploaded document list

I strongly recommend this component.

## `DocumentList.tsx`

```tsx
import {
  List,
  Typography,
} from 'antd';

import {
  FilePdfOutlined,
  FileOutlined,
} from '@ant-design/icons';

import './DocumentList.scss';

interface DocumentListProps {
  documents: string[];
  loading?: boolean;
}

export function DocumentList({
  documents,
  loading = false,
}: DocumentListProps) {
  return (
    <section className="document-list">
      <Typography.Title level={3}>
        Uploaded Resources
      </Typography.Title>

      <List
        loading={loading}
        bordered
        dataSource={documents}
        locale={{
          emptyText:
            'No documents have been uploaded yet.',
        }}
        renderItem={(filename) => (
          <List.Item>
            <List.Item.Meta
              avatar={
                filename
                  .toLowerCase()
                  .endsWith('.pdf')
                  ? <FilePdfOutlined />
                  : <FileOutlined />
              }
              title={filename}
            />
          </List.Item>
        )}
      />
    </section>
  );
}
```

---

# 13. Document hook

Instead of having `App.tsx` call the API directly:

## `useDocuments.ts`

```typescript
import {
  useCallback,
  useEffect,
  useState,
} from 'react';

import {
  getDocuments,
} from '../api/document.api';

export function useDocuments() {
  const [documents, setDocuments] =
    useState<string[]>([]);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const loadDocuments =
    useCallback(async () => {
      try {
        setLoading(true);
        setError(null);

        const result =
          await getDocuments();

        setDocuments(result);
      } catch {
        setError(
          'Unable to load uploaded documents.',
        );
      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    void loadDocuments();
  }, [loadDocuments]);

  return {
    documents,
    loading,
    error,
    reload: loadDocuments,
  };
}
```

---

# 14. Question API

## `question.api.ts`

```typescript
import { api } from './api';

import type {
  GeneratedQuestionsResponse,
  InterviewQuestion,
} from '../types/question';

export async function generateQuestions(): Promise<
  InterviewQuestion[]
> {
  const response =
    await api.post<GeneratedQuestionsResponse>(
      '/rest/v1/questions/generate',
    );

  return response.data.items;
}

export async function saveQuestions(
  questions: InterviewQuestion[],
): Promise<void> {
  await api.post(
    '/rest/v1/questions',
    {
      items: questions,
    },
  );
}
```

---

# 15. Questions hook

## `useQuestions.ts`

```typescript
import {
  useState,
} from 'react';

import {
  generateQuestions,
  saveQuestions,
} from '../api/question.api';

import type {
  InterviewQuestion,
} from '../types/question';

export function useQuestions() {
  const [questions, setQuestions] =
    useState<InterviewQuestion[]>([]);

  const [generating, setGenerating] =
    useState(false);

  const [saving, setSaving] =
    useState(false);

  const generate = async () => {
    try {
      setGenerating(true);

      const result =
        await generateQuestions();

      setQuestions(result);
    } finally {
      setGenerating(false);
    }
  };

  const save = async () => {
    if (questions.length === 0) {
      return;
    }

    try {
      setSaving(true);

      await saveQuestions(questions);
    } finally {
      setSaving(false);
    }
  };

  return {
    questions,
    generating,
    saving,
    generate,
    save,
  };
}
```

---

# 16. Questions table

## `QuestionsTable.tsx`

```tsx
import { Table } from 'antd';

import type {
  InterviewQuestion,
} from '../../types/question';

interface QuestionsTableProps {
  questions: InterviewQuestion[];
}

export function QuestionsTable({
  questions,
}: QuestionsTableProps) {
  return (
    <Table<InterviewQuestion>
      rowKey={(record, index) =>
        record.id?.toString()
        ?? `generated-${index}`
      }
      columns={[
        {
          title: '#',
          width: 70,
          render: (
            _value,
            _record,
            index,
          ) => index + 1,
        },
        {
          title: 'Interview Question',
          dataIndex: 'question',
          key: 'question',
        },
        {
          title: 'Short Answer',
          dataIndex: 'shortAnswer',
          key: 'shortAnswer',
        },
        {
          title: 'Tag',
          dataIndex: 'tag',
          key: 'tag',
        },
      ]}
      dataSource={questions}
      pagination={{
        pageSize: 5,
        showSizeChanger: false,
        showQuickJumper: true,
      }}
      locale={{
        emptyText:
          'Your questions will be here',
      }}
    />
  );
}
```

This gives you the requested pagination immediately.

---

# 17. Question generator

## `QuestionGenerator.tsx`

```tsx
import {
  Button,
  Divider,
  message,
  Typography,
} from 'antd';

import {
  useQuestions,
} from '../../hooks/useQuestions';

import {
  QuestionsTable,
} from './QuestionsTable';

import './Questions.scss';

export function QuestionGenerator() {
  const {
    questions,
    generating,
    saving,
    generate,
    save,
  } = useQuestions();

  const handleGenerate =
    async () => {
      try {
        await generate();

        message.success(
          'Interview questions generated successfully.',
        );
      } catch {
        message.error(
          'Failed to generate interview questions.',
        );
      }
    };

  const handleSave =
    async () => {
      try {
        await save();

        message.success(
          'Questions saved successfully.',
        );
      } catch {
        message.error(
          'Failed to save questions.',
        );
      }
    };

  return (
    <section className="questions">
      <Divider />

      <div className="questions__generate">
        <Button
          type="primary"
          size="large"
          loading={generating}
          onClick={handleGenerate}
        >
          GENERATE QUESTIONS
        </Button>
      </div>

      <Typography.Title level={2}>
        Generated Interview Questions
      </Typography.Title>

      <QuestionsTable
        questions={questions}
      />

      <Divider />

      <div className="questions__save">
        <Typography.Text>
          Save questions in TI Knowledge Platform?
        </Typography.Text>

        <Button
          size="large"
          type="primary"
          disabled={questions.length === 0}
          loading={saving}
          onClick={handleSave}
        >
          SAVE QUESTIONS
        </Button>
      </div>
    </section>
  );
}
```

---

# 18. Questions styling

```scss
.questions {
  padding: 0 0 2rem;

  &__generate {
    display: flex;
    justify-content: center;

    margin: 2rem 0 2.5rem;

    button {
      min-width: 320px;
      height: 60px;

      font-size: 1.05rem;
      font-weight: 600;
    }
  }

  &__save {
    display: flex;
    flex-direction: column;
    align-items: center;

    gap: 1rem;

    padding: 1rem 0 2rem;
  }
}
```

---

# 19. Header

Since both UIs belong to the same platform, I would make their headers almost identical.

For `ti-ai-question-ui`:

```tsx
import './Header.scss';

export function Header() {
  return (
    <header className="app-header">
      <a
        href="/"
        className="app-header__brand"
      >
        <div className="app-header__logo">
          TI
        </div>

        <span className="app-header__title">
          TI AI Question Generator
        </span>
      </a>

      <div className="app-header__actions">
        AI Knowledge Platform
      </div>
    </header>
  );
}
```

And use essentially the same SCSS as your existing `ti-knowledge-ui`.

---

# 20. Footer

```tsx
import './Footer.scss';

export function Footer() {
  return (
    <footer className="app-footer">
      <span>
        TI Knowledge Platform
      </span>

      <span>
        AI Question Generator
      </span>
    </footer>
  );
}
```

---

# 21. App.tsx

Now the application becomes very clean:

```tsx
import {
  ConfigProvider,
} from 'antd';

import {
  Header,
} from './components/layout/Header';

import {
  Footer,
} from './components/layout/Footer';

import {
  DocumentUpload,
} from './components/upload/DocumentUpload';

import {
  DocumentList,
} from './components/documents/DocumentList';

import {
  QuestionGenerator,
} from './components/questions/QuestionGenerator';

import {
  useDocuments,
} from './hooks/useDocuments';

import './App.scss';

function App() {
  const {
    documents,
    loading,
    reload,
  } = useDocuments();

  return (
    <ConfigProvider>
      <div className="app">
        <Header />

        <main className="app__content">
          <DocumentUpload
            onUploaded={reload}
          />

          <QuestionGenerator />

          <DocumentList
            documents={documents}
            loading={loading}
          />
        </main>

        <Footer />
      </div>
    </ConfigProvider>
  );
}

export default App;
```

---

# 22. App.scss

```scss
.app {
  min-height: 100vh;

  display: flex;
  flex-direction: column;

  background: #fff;

  &__content {
    width: 100%;
    max-width: 1400px;

    flex: 1;

    margin: 0 auto;
    padding: 0 2rem 3rem;
  }
}
```

---

# 23. index.scss

```scss
* {
  box-sizing: border-box;
}

html,
body,
#root {
  min-height: 100%;
  margin: 0;
}

body {
  font-family:
    Inter,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;

  color: #0f172a;
  background: #fff;
}

button,
input,
textarea {
  font: inherit;
}
```

---

# 24. Main.tsx

```tsx
import {
  StrictMode,
} from 'react';

import {
  createRoot,
} from 'react-dom/client';

import 'antd/dist/reset.css';

import './index.scss';

import App from './App';

createRoot(
  document.getElementById('root')!,
).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
```

---

# 25. Final UI flow

The second application now works like this:

```text
                  ti-ai-question-ui
                         │
                         ▼
              ┌─────────────────────┐
              │ Upload documents    │
              └──────────┬──────────┘
                         │
                         ▼
                  ti-import-api
                         │
                         ▼
                 ti-import-worker
                         │
                         ▼
                     PGVector
                         │
                         │
             ┌───────────┴────────────┐
             │                        │
             ▼                        ▼
      GET /documents          Generate Questions
             │                        │
             ▼                        ▼
       filename list          Document AI Agent
                                      │
                                      ▼
                                   PGVector
                                      │
                                      ▼
                              InterviewQuestion[]
                                      │
                                      ▼
                                React Table
                                      │
                                      ▼
                                  [ SAVE ]
                                      │
                                      ▼
                            TI Knowledge DB
```

This is much cleaner than putting uploading and question generation inside `ti-chatbot-ui`.

---

# 26. What happens to SSE?

I would **remove SSE from `ti-ai-question-ui` for the first implementation** unless question generation itself needs streaming.

There are actually two different asynchronous processes:

### Document upload

```text
UI
 │
 │ POST multipart
 ▼
ti-import-api
 │
 ▼
RabbitMQ
 │
 ▼
ti-import-worker
 │
 ▼
PGVector
```

The UI can simply poll/refresh the uploaded-document list or receive an explicit completion response, depending on your existing BE contract.

### Question generation

```text
UI
 │
 │ POST generate
 ▼
ti-orchestrator-api
 │
 ▼
Document AI Agent
 │
 ▼
PGVector
 │
 ▼
LLM
 │
 ▼
InterviewQuestion[]
 │
 ▼
UI
```

If generation takes long enough that you need streaming/progress updates, then we can add SSE specifically to the generation workflow.

But I would **not reuse the chatbot's SSE channel blindly**.

---

# 27. `ti-chatbot-ui` becomes much simpler

The existing repository:

[ti-chatbot-ui on GitHub](https://github.com/MarinaPimenova/ti-chatbot-ui?utm_source=chatgpt.com)

should eventually contain roughly:

```text
ti-chatbot-ui/
└── src/
    ├── api/
    │   └── chat.api.ts
    │
    ├── components/
    │   ├── layout/
    │   │   ├── Header/
    │   │   └── Footer/
    │   │
    │   └── chat/
    │       ├── ChatWindow.tsx
    │       ├── ChatMessage.tsx
    │       └── ChatInput.tsx
    │
    ├── hooks/
    │   └── useChatSse.ts
    │
    ├── types/
    │   └── chat.ts
    │
    ├── App.tsx
    └── main.tsx
```

No:

```text
DocumentUpload
QuestionGenerator
QuestionsTable
DocumentList
```

Those belong to `ti-ai-question-ui`.

---

# 28. Structured responses should be shared conceptually

There is still one important relationship between the applications.

The orchestrator can produce:

```text
type = text
```

for chatbot responses,

or:

```text
type = filename
```

for:

```text
list all uploaded documents
```

or:

```text
type = interview-question
```

for question generation.

But **the question UI doesn't need to understand chatbot messages**.

Its API can simply expect:

```typescript
GeneratedQuestionsResponse
```

This keeps the frontend strongly typed and avoids creating a giant generic AI response model.

---

# 29. Recommended backend ownership

I would also make the ownership explicit:

| Function                     | UI                  | Backend                       |
| ---------------------------- | ------------------- | ----------------------------- |
| Chat                         | `ti-chatbot-ui`     | `ti-orchestrator-api`         |
| RAG answer                   | `ti-chatbot-ui`     | Document AI Agent             |
| SQL answer                   | `ti-chatbot-ui`     | SQL AI Agent                  |
| Upload resource              | `ti-ai-question-ui` | `ti-import-api` / worker      |
| List resources               | `ti-ai-question-ui` | `ti-knowledge-api` / PGVector |
| Generate interview questions | `ti-ai-question-ui` | `ti-orchestrator-api`         |
| Display generated questions  | `ti-ai-question-ui` | —                             |
| Save questions               | `ti-ai-question-ui` | `ti-knowledge-api`            |

That separation is architecturally much healthier.

---

# 30. One change I would make to the previous design

Previously I suggested:

```text
DocumentUpload
    ↓
QuestionsSection
    ↓
ChatWindow
```

I would **no longer do that**.

Instead:

### `ti-ai-question-ui`

```text
Header
   ↓
Document Upload
   ↓
Generate Questions
   ↓
Questions Table
   ↓
Save Questions
   ↓
Uploaded Resources
   ↓
Footer
```

### `ti-chatbot-ui`

```text
Header
   ↓
Chat
   ↓
Footer
```

This gives us two very clear applications with almost no UI responsibility overlap.

---

# 31. Suggested repositories

I would therefore have:

```text
GitHub
│
├── ti-knowledge-ui
│
├── ti-chatbot-ui
│
└── ti-ai-question-ui       ← NEW
```

And backend:

```text
Backend
│
├── ti-gateway-api
├── ti-orchestrator-api
├── ti-knowledge-api
├── ti-import-api
├── ti-import-worker
├── ti-export-api
├── ti-audit-api
└── ti-notification-api
```

The conceptual relationship becomes:

```text
                    ┌──────────────────────┐
                    │   ti-knowledge-ui    │
                    │ Existing application │
                    └──────────┬───────────┘
                               │
                               │
       ┌───────────────────────┼────────────────────────┐
       │                       │                        │
       ▼                       ▼                        ▼
┌───────────────┐     ┌─────────────────┐      ┌──────────────┐
│ti-chatbot-ui  │     │ti-ai-question-ui│      │ Other UIs    │
│               │     │                 │      │              │
│ Chat / RAG    │     │ Upload / AI Q's │      │              │
└───────┬───────┘     └────────┬────────┘      └──────────────┘
        │                      │
        └───────────┬──────────┘
                    ▼
           ti-gateway-api
                    │
                    ▼
          ti-orchestrator-api
             │            │
             ▼            ▼
       Document AI     SQL AI
             │
             ▼
           PGVector
```

**This is the architecture I would proceed with.**

And for the second application, I would use **`ti-ai-question-ui`** rather than `ti-ai-admin-ui`: it communicates the actual business purpose without implying that the whole application is an administration console.

[1]: https://github.com/MarinaPimenova/ti-chatbot-ui "GitHub - MarinaPimenova/ti-chatbot-ui: AI Chatbot UI · GitHub"
[2]: https://vite.dev/guide/?utm_source=chatgpt.com "Getting Started | Vite"
[3]: https://github.com/vitejs/vite/blob/main/docs/guide/features.md?utm_source=chatgpt.com "vite/docs/guide/features.md at main · vitejs/vite · GitHub"
