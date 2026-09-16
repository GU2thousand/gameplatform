[English](#english) | [简体中文](#简体中文)

<a id="english"></a>

# English

# AI Gamified Career Training Platform

<img width="1209" height="926" alt="52121773002743_ pic" src="https://github.com/user-attachments/assets/1242cc46-d777-45ef-aaad-84ccfa45ec7a" />

## What the project does

An AI-powered platform for product management and software engineering interview practice.

Users complete AI-generated practical challenges such as PRD writing, system design, and API design. The platform evaluates submitted answers and provides:

- Scores across rubric dimensions
- Actionable feedback
- XP accumulation
- Salary tier progression
- Recommendations for interview preparation

The current static frontend supports:

- Quest Generator
- Submit Answer
- Automatic creation of the current session user through an implicit frontend call
- Custom challenge inputs
- Markdown rendering of challenge briefs and evaluation summaries

The project addresses fragmented interview practice by providing tasks and actionable feedback that resemble real PM/SDE workflows.

## Tech stack

Backend:

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Boot Actuator

Databases:

- H2
- PostgreSQL
- MySQL

AI layer:

- LangChain4j
- OpenAI
- Local fallback mode for demonstrations without an API key

Runtime and deployment:

- Maven Wrapper
- Docker
- Docker Compose
- Render

## System architecture

Overall flow:

Frontend UI<br>
-> REST API<br>
-> AI orchestration layer<br>
-> local heuristic engine / LangChain4j + OpenAI<br>
-> relational database

Current modules:

- **Challenge generation:** generates context, requirements, constraints, and acceptance criteria; accepts `track`, `challengeType`, `focusGoal`, and `businessContext` inputs.
- **Submission evaluation:** processes and evaluates submitted answers.
- **Rubric scoring:** calculates the final score using weighted dimensions.
- **Gamification:** manages XP and salary tier progression.
- **Recommendation:** suggests interview preparation based on weaker areas.
- **Deployment and debugging:** provides health checks, AI mode diagnostics, and profile configuration.

Main endpoints:

- `POST /api/user`
- `POST /api/challenge/generate`
- `POST /api/submission`
- `GET /api/user/{id}/progress`
- `GET /api/debug/ai-mode`
- `POST /api/debug/ai-mode`
- `GET /actuator/health`

## Run locally

### 1. Requirements

- Java 17+
- Docker, if you want to run PostgreSQL

### 2. Install dependencies and run tests

```bash
./mvnw test
```

### 3. Simplest setup: H2 + local AI

```bash
./mvnw spring-boot:run
```

After startup, open:

- `http://localhost:8080/`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/api/debug/ai-mode`

Notes:

- The default database is `H2`.
- The default AI provider is `local`.
- The homepage uses a simplified two-panel flow: `Quest Generator -> Submit Answer`.

### 4. Run locally with PostgreSQL

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Then start the application:

```bash
SPRING_PROFILES_ACTIVE=postgres \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
./mvnw spring-boot:run
```

`docker-compose.yml` exposes PostgreSQL on host port `5433` by default.

### 5. Run with PostgreSQL + LangChain4j

```bash
SPRING_PROFILES_ACTIVE=postgres,langchain4j \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
OPENAI_API_KEY='your_openai_key' \
OPENAI_MODEL='gpt-4o-mini' \
./mvnw spring-boot:run
```

After startup, check the active mode:

```bash
curl http://localhost:8080/api/debug/ai-mode
```

The following response values indicate that LangChain4j is active:

- `provider = langchain4j`
- `challengeClient = LangChain4jChallengeAiClient`
- `evaluationClient = LangChain4jEvaluationAiClient`

### 6. Verify custom challenge inputs

```bash
curl -X POST http://localhost:8080/api/challenge/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "difficulty":"INTERMEDIATE",
    "roleTrack":"PM + SDE",
    "challengeType":"API Design",
    "focusGoal":"latency reduction and rollout safety",
    "businessContext":"A fintech app is seeing slow balance lookups during market open.",
    "customRequirements":["Include rollout metrics and monitoring checkpoints."],
    "customConstraints":["Must stay under 150ms p95."],
    "customAcceptanceCriteria":["Explain how success will be measured after rollout."]
  }'
```

If the returned `title/context/requirements/constraints/acceptanceCriteria` include the custom information, the frontend-to-backend custom generation flow is working.

## Additional documentation

- Detailed design and deployment: `AI_Gamified_Career_Training_Platform_README.md`
- Local environment variable template: `.env.example`
- Local PostgreSQL configuration: `docker-compose.yml`

---

<a id="简体中文"></a>

# 简体中文

# AI Gamified Career Training Platform
<img width="1209" height="926" alt="52121773002743_ pic" src="https://github.com/user-attachments/assets/1242cc46-d777-45ef-aaad-84ccfa45ec7a" />

## 项目做什么

这是一个面向产品经理和工程师面试训练的 AI 驱动平台。

用户在平台上完成 AI 生成的实战挑战，例如：

- PRD 撰写
- 系统设计
- API 设计

系统会对用户提交的答案进行结构化评分，并根据结果完成：

- rubric 维度打分
- 反馈建议生成
- XP 累积
- 薪资等级晋级
- 面试准备方向推荐

当前静态前端已经支持：

- Quest Generator
- Submit Answer
- 自动创建当前会话用户（前端隐式调用）
- 自定义 challenge 输入
- 以 Markdown 形式展示 challenge brief 和 evaluation summary

这个项目要解决的问题是：传统刷题平台过于碎片化，缺少接近真实 PM/SDE 工作流的任务训练和可执行反馈。

## 技术栈

后端：

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Boot Actuator

数据库：

- H2
- PostgreSQL
- MySQL

AI 层：

- LangChain4j
- OpenAI
- local fallback mode（无 API Key 时可本地演示）

运行与部署：

- Maven Wrapper
- Docker
- Docker Compose
- Render

## 系统架构

整体链路：

Frontend UI<br>
-> REST API<br>
-> AI orchestration layer<br>
-> local heuristic engine / LangChain4j + OpenAI<br>
-> relational database

当前模块划分：

- challenge generation
  - 生成挑战题，包括背景、要求、约束、验收标准
  - 支持用户输入 `track`、`challengeType`、`focusGoal`、`businessContext`
- submission evaluation
  - 处理答案提交与评分
- rubric scoring
  - 按维度加权计算最终分数
- gamification
  - 管理 XP 与 salary tier progression
- recommendation
  - 根据弱项返回 interview prep 建议
- deployment and debug
  - 提供 health check、AI mode debug、profile 配置

主要接口：

- `POST /api/user`
- `POST /api/challenge/generate`
- `POST /api/submission`
- `GET /api/user/{id}/progress`
- `GET /api/debug/ai-mode`
- `POST /api/debug/ai-mode`
- `GET /actuator/health`

## 如何本地启动

### 1. 环境要求

- Java 17+
- Docker（如果你要跑 PostgreSQL）

### 2. 安装依赖并运行测试

```bash
./mvnw test
```

### 3. 最简单启动方式：H2 + local AI

```bash
./mvnw spring-boot:run
```

启动后访问：

- `http://localhost:8080/`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/api/debug/ai-mode`

说明：

- 默认数据库是 `H2`
- 默认 AI provider 是 `local`
- 首页现在是简化后的双面板流程：`Quest Generator -> Submit Answer`

### 4. 使用 PostgreSQL 本地启动

先启动 PostgreSQL：

```bash
docker compose up -d postgres
```

再启动应用：

```bash
SPRING_PROFILES_ACTIVE=postgres \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
./mvnw spring-boot:run
```

说明：

- `docker-compose.yml` 默认把 PostgreSQL 暴露在宿主机 `5433`

### 5. 使用 PostgreSQL + LangChain4j 启动

```bash
SPRING_PROFILES_ACTIVE=postgres,langchain4j \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
OPENAI_API_KEY='your_openai_key' \
OPENAI_MODEL='gpt-4o-mini' \
./mvnw spring-boot:run
```

启动后可用下面的接口确认当前模式：

```bash
curl http://localhost:8080/api/debug/ai-mode
```

如果返回里包含以下内容，说明 LangChain4j 已生效：

- `provider = langchain4j`
- `challengeClient = LangChain4jChallengeAiClient`
- `evaluationClient = LangChain4jEvaluationAiClient`

### 6. 验证自定义 challenge 输入

```bash
curl -X POST http://localhost:8080/api/challenge/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "difficulty":"INTERMEDIATE",
    "roleTrack":"PM + SDE",
    "challengeType":"API Design",
    "focusGoal":"latency reduction and rollout safety",
    "businessContext":"A fintech app is seeing slow balance lookups during market open.",
    "customRequirements":["Include rollout metrics and monitoring checkpoints."],
    "customConstraints":["Must stay under 150ms p95."],
    "customAcceptanceCriteria":["Explain how success will be measured after rollout."]
  }'
```

如果返回的 `title/context/requirements/constraints/acceptanceCriteria` 中包含这些自定义信息，说明前后端自定义生成链路已经生效。

## 补充说明

- 详细设计与部署说明见 `AI_Gamified_Career_Training_Platform_README.md`
- 本地环境变量模板见 `.env.example`
- 本地 PostgreSQL 配置见 `docker-compose.yml`
