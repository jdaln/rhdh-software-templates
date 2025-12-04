# Quarkus AI Chatbot Template

A Red Hat Developer Hub (RHDH) template for creating AI Chatbot services using **Quarkus** and **LangChain4j** with streaming support!

## Features

- 🤖 **AI Chat Interface** - Modern, responsive web UI with KCD Suisse Romande branding
- 🔗 **LangChain4j Integration** - Declarative AI services with the Quarkus LangChain4j extension
- 📡 **Streaming Support** - Server-Sent Events (SSE) for real-time token streaming
- 🚀 **OpenAI-Compatible API** - Works with any OpenAI-compatible LLM endpoint (DeepSeek, OpenAI, etc.)
- ☸️ **Kubernetes Ready** - Includes Helm charts and ArgoCD configuration
- 🔐 **Secure Configuration** - LLM credentials managed via Kubernetes secrets
- ☕ **Java 21** - Uses latest Java LTS version
- 🐳 **Container Ready** - Dockerfile with UBI9 OpenJDK 21 base image

## Default Configuration

This template is pre-configured to use:

| Setting | Value |
|---------|-------|
| **Quarkus Version** | 3.30.1 |
| **Java Version** | 21 |
| **LLM API** | DeepSeek R1 Qwen 14B (OpenAI-compatible) |
| **Base URL** | `https://deepseek-r1-qwen-14b-w4a16-maas-apicast-production.apps.prod.rhoai.rh-aiservices-bu.com:443` |
| **Model** | `r1-qwen-14b-w4a16` |

## Usage

This template creates:
1. A Quarkus application with LangChain4j for AI/LLM integration
2. REST API endpoints:
   - `/api/chat` - Non-streaming chat endpoint
   - `/api/chat/stream` - Streaming chat endpoint (SSE)
   - `/api/chat/config` - Configuration endpoint
3. A beautiful chat web interface with KCD Suisse Romande branding
4. Helm charts for deployment with LLM secret management
5. ArgoCD resources for GitOps deployment

## LLM Configuration

The template supports any OpenAI-compatible LLM API. Configure via Kubernetes secrets:

```bash
kubectl create secret generic ai-chatbot-secrets-llm \
  --from-literal=LLM_API_BASE_URL=https://deepseek-r1-qwen-14b-w4a16-maas-apicast-production.apps.prod.rhoai.rh-aiservices-bu.com:443 \
  --from-literal=LLM_API_KEY=<your-api-key> \
  --from-literal=MODEL_NAME=r1-qwen-14b-w4a16
```

Then reference the secret in your Helm values:

```yaml
llm:
  enabled: true
  secretName: ai-chatbot-secrets-llm
```

## Project Structure

```
skeleton/
├── src/main/java/${{values.java_package_name}}/
│   ├── chat/
│   │   ├── ai/ChatAiService.java      # LangChain4j AI service interface
│   │   ├── config/ChatConfig.java     # Configuration interface
│   │   ├── model/                      # Data models
│   │   ├── resource/ChatResource.java # REST endpoints
│   │   └── service/ChatService.java   # Business logic
│   └── GreetingResource.java          # Health check endpoint
├── src/main/resources/
│   ├── application.properties         # Quarkus & LLM configuration
│   └── META-INF/resources/
│       ├── index.html                  # Chat web interface
│       ├── styles.css                  # Styling
│       └── app.js                      # Frontend logic
└── src/main/docker/
    └── Dockerfile.jvm                  # Container image (Java 21)
```

## Build and Deploy

The template includes:
- **Maven Wrapper** - For consistent builds
- **Dockerfile.jvm** - Uses UBI9 OpenJDK 21 base image
- **Tekton Pipeline** - Automated build and deployment
- **Helm Charts** - For Kubernetes deployment
- **ArgoCD** - GitOps configuration

## Documentation

- [Quarkus LangChain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)
- [Quarkus Extensions](https://quarkus.io/extensions/io.quarkiverse.langchain4j/quarkus-langchain4j-core/)
- [LangChain4j GitHub](https://github.com/langchain4j/langchain4j)

