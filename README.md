# AI Gamified Career Training Platform

An AI-powered interview training platform where users solve product and engineering challenges, receive structured feedback, and progress through salary-style levels.

## What This Project Solves

Most interview practice tools focus on short Q&A. This project targets more realistic workflow training:

- Generate PM/SDE-style challenges such as PRD writing, system design, and API design
- Evaluate submissions with rubric-based scoring
- Turn scores into XP and salary-tier progression
- Recommend interview prep topics based on weakest dimensions

## Current Stack

- Backend: Java 17, Spring Boot 3
- API: Spring Web, Bean Validation
- Persistence: Spring Data JPA
- Database: H2, PostgreSQL, MySQL
- AI Layer: local fallback mode or LangChain4j + OpenAI
- Health Checks: Spring Boot Actuator
- Deployment: Docker, Render-ready

## Architecture

Frontend UI (static starter)
-> REST API
-> AI orchestration layer
-> Local heuristic engine or LangChain4j/OpenAI
-> Relational database

Core modules:

- challenge generation
- submission evaluation
- rubric scoring
- XP and salary-tier progression
- recommendation engine
- deployment/debug tooling

## Run Locally

### 1. Run tests

```bash
./mvnw test
```

### 2. Fastest local mode: H2 + local AI

```bash
./mvnw spring-boot:run
```

Open:

- `http://localhost:8080/`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/api/debug/ai-mode`

### 3. PostgreSQL with Docker Compose

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the app against PostgreSQL:

```bash
SPRING_PROFILES_ACTIVE=postgres \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
./mvnw spring-boot:run
```

Note:

- Docker Compose exposes PostgreSQL on host port `5433` by default
- Change it with `POSTGRES_HOST_PORT` if needed

### 4. PostgreSQL + LangChain4j

```bash
SPRING_PROFILES_ACTIVE=postgres,langchain4j \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
OPENAI_API_KEY='<your_key>' \
OPENAI_MODEL='gpt-4o-mini' \
./mvnw spring-boot:run
```

Verification:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/debug/ai-mode
```

## Configuration Modes

- Default: `H2 + local AI`
- `postgres`: PostgreSQL datasource
- `mysql`: MySQL datasource
- `langchain4j`: LangChain4j/OpenAI provider

Useful env vars:

- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_AI_PROVIDER`
- `OPENAI_API_KEY`
- `OPENAI_MODEL`
- `OPENAI_BASE_URL`

Example env template:

- `.env.example`

## Main API Endpoints

- `POST /api/user`
- `POST /api/challenge/generate`
- `POST /api/submission`
- `GET /api/user/{id}/progress`
- `GET /api/debug/ai-mode`
- `POST /api/debug/ai-mode`
- `GET /actuator/health`

## Deployment

This repo is ready for Render deployment with the included `Dockerfile`.

Recommended production setup:

- Runtime: Docker
- Health check path: `/actuator/health`
- Database: external PostgreSQL or MySQL
- AI mode: `local` for demo or `langchain4j` with a real OpenAI key

Example Render env vars:

```bash
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
APP_AI_PROVIDER=local
```

## Repo Files

- `README.md`: GitHub homepage overview
- `AI_Gamified_Career_Training_Platform_README.md`: detailed architecture and setup guide
- `docker-compose.yml`: local PostgreSQL and app container setup
- `.env.example`: local environment template

## Status

Current project state:

- local mode works
- PostgreSQL profile works
- LangChain4j integration is wired and profile-driven
- health checks are exposed
- debug endpoint shows the active AI mode

For a full LLM-backed flow, you still need a real `OPENAI_API_KEY`.
