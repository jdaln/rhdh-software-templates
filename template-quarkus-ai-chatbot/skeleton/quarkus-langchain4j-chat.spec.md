# Quarkus LangChain4j Chat – Spec

## 0. Tessl Context

### 0.1 Spec

This document is the **Spec** for the project: a Markdown file (`.spec.md`) that defines the software’s intent and capabilities. It is the **source of truth** for:

- What the app does (requirements).
- How it integrates with Quarkus + LangChain4j.
- How it uses a self-hosted, OpenAI-compatible model.

### 0.2 Usage Specs (Dependencies)

This project depends on several libraries/frameworks. Each should be backed by a **Usage Spec** describing how to use it (APIs, configuration, common patterns).

Planned usage specs:

1. **Quarkus Core**  
   - Name: `usage.quarkus.core`  
   - Scope: Quarkus 3.x fundamentals (dev mode, config, RESTEasy Reactive, static resources, health/metrics).

2. **Quarkus LangChain4j + OpenAI**  
   - Name: `usage.quarkus.langchain4j-openai`  
   - Scope: `quarkus-langchain4j-openai` extension, `@RegisterAiService`, chat and streaming integration, configuration keys.

3. **OpenAI-Compatible Chat API**  
   - Name: `usage.openai.chat-compatible`  
   - Scope: OpenAI-style `/v1/chat/completions` endpoint (request/response schema, streaming protocol).

4. **Browser HTML/JS Frontend**  
   - Name: `usage.web.frontend-basic`  
   - Scope: Single-page HTML, fetch-stream API, SSE-style parsing.

### 0.3 Knowledge Index

The project will have a `KNOWLEDGE.md` Knowledge Index that links to the installed usage specs:

```md
# KNOWLEDGE

- [Quarkus Core](usage.quarkus.core.spec.md)
- [Quarkus LangChain4j + OpenAI](usage.quarkus.langchain4j-openai.spec.md)
- [OpenAI-Compatible Chat API](usage.openai.chat-compatible.spec.md)
- [Basic Web Frontend](usage.web.frontend-basic.spec.md)
```

### 0.4 Workspace

- **Workspace** = the project repository containing:
  - This spec (`quarkus-langchain4j-chat.spec.md`).
  - `KNOWLEDGE.md`.
  - Generated + hand-written code (`src/main/java`, `src/main/resources`).
  - Quarkus config (`application.properties`, `pom.xml`).

---

## 1. Overview

Build a **simple Quarkus-based AI chat application** using **LangChain4j** and the **Quarkus LangChain4j OpenAI extension**, closely following patterns from the **Quarkus LangChain4j Workshop** (AI-infused app, streaming responses, system messages).

The app provides:

- A **single HTML/JS chat UI** served by Quarkus.
- A backend using **Java 21** on the **latest Quarkus 3.x**.
- Integration with a **self-hosted, OpenAI-compatible model** (URL, model name, API key).
- **Token-by-token streaming** of responses to the browser.

---

## 2. Goals & Non-goals

### 2.1 Goals

- Single-page chat UI with:
  - Conversation history.
  - Streaming answers (token-by-token).
- Use **Quarkus standards**:
  - RESTEasy Reactive.
  - CDI beans.
  - Quarkus config and health endpoints.
- Use **LangChain4j + Quarkus extension** for LLM integration.
- Configuration-driven model connection:
  - Base URL.
  - Model name.
  - API key.
- All conversation state kept **client-side** (no database).

### 2.2 Non-goals

- No user authentication or authorization.
- No long-term storage of chats.
- No RAG, tools, MCP, or multi-agent workflows.
- No external storage.

---

## 3. Personas & User Journeys

### 3.1 Persona

- **Developer / Architect (Primary User)**:
  - Runs the self-hosted LLM.
  - Has the model `base-url`, `model-name`, and `api-key`.
  - Wants a simple browser UI to interact with the model and experiment.

### 3.2 User Journeys

1. **Configure model**  
2. **Chat with model**  
3. **Handle errors**

---

## 4. Functional Requirements

### 4.1 Configuration (Quarkus)

- Configure via:
  - `quarkus.langchain4j.openai.base-url`
  - `quarkus.langchain4j.openai.api-key`
  - `quarkus.langchain4j.openai.chat-model.model-name`
- App-specific:
  - `ai.chat.system-prompt`
  - `ai.chat.timeout`
- Must support env var overrides.
- API key must not be logged.

---

### 4.2 LLM Integration (LangChain4j)

- Use `quarkus-langchain4j-openai` extension.
- Create `ChatAiService` interface with `@RegisterAiService`.
- Provide:
  - Non-streaming chat method.
  - Streaming chat method using LangChain4j streaming API.
- System prompt must be included in history.

---

### 4.3 REST API

#### `/api/chat` (non-streaming)
- Request: `{ messages, newMessage, systemPrompt? }`
- Response: `{ assistantMessage, usage? }`

#### `/api/chat/stream` (streaming)
- SSE-style streamed tokens.
- Events:
  - `token`
  - `done`
  - `error`

#### Optional `/api/config`
- Returns model name and system prompt.

#### Health
- `/q/health/live`
- `/q/health/ready`

---

## 5. Frontend

### 5.1 Structure

- Static files:
  - `index.html`
  - `styles.css`
  - `app.js`
- Located in `META-INF/resources`.

### 5.2 Behavior

- JS maintains an in-memory array of messages.
- On Send:
  - Append user message.
  - POST `/api/chat/stream`.
- Streaming parsing via `fetch` + streamed reader.
- Stop button cancels stream.
- Clear button resets conversation.

---

## 6. Non-Functional Requirements

- Fast streaming token display.
- No logging sensitive content.
- Same-origin deployment.
- Quarkus health/metrics for observability.

---

## 7. Architecture

### Components

- **Frontend**
- **REST layer**
- **ChatService**
- **ChatAiService** (LangChain4j)
- **Config classes**

### Sequence (Streaming)

1. User sends text  
2. UI posts `/api/chat/stream`  
3. Backend builds message list  
4. LangChain4j streams tokens  
5. Backend forwards tokens  
6. UI updates assistant message live  

---

## 8. Configuration Example

```properties
quarkus.langchain4j.openai.base-url=https://my-llm.example.com
quarkus.langchain4j.openai.api-key=${OPENAI_API_KEY}
quarkus.langchain4j.openai.chat-model.model-name=my-hosted-model
ai.chat.system-prompt=You are a helpful assistant.
ai.chat.timeout=60
```


