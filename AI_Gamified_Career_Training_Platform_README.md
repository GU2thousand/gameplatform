# AI-Powered Gamified Career Training Platform

## Overview

This project is an AI-driven gamified learning platform that simulates real-world product and engineering workflows.

Users complete AI-generated challenges such as PRD drafting, system design, and API specification. Submissions are evaluated by an LLM-style scoring engine using structured rubrics. Performance determines progression across simulated salary tiers.

The platform goals:

- Simulate realistic PM/SDE collaboration
- Provide structured evaluation feedback
- Model skill growth through quantified scoring
- Increase engagement via gamified progression

---

## Architecture

### High-Level Flow

Frontend UI (static starter / Vue-ready)
-> REST API (Spring Boot)
-> AI Orchestration Layer (pluggable client interfaces)
-> LLM Provider (OpenAI / Azure in production)
-> Database (H2 local, PostgreSQL/MySQL profiles for deployment)

### Current Backend Modules

- `challenge generation`
  - Generates quests with context, constraints, acceptance criteria, and expected output format.
  - Supports difficulty levels: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`.
- `evaluation and scoring`
  - Scores submissions on 5 rubric dimensions.
  - Uses weighted formula: `FinalScore = sum(weight_i * rubric_i_score)`.
- `gamification`
  - Tracks XP and computes salary tier progression.
- `recommendation`
  - Detects weakest rubric dimension and returns improvement tracks.

### API Endpoints

- `POST /api/user`
  - Create user.
- `POST /api/challenge/generate`
  - Generate a quest.
- `POST /api/submission`
  - Submit answer and trigger evaluation.
- `GET /api/user/{id}/progress`
  - Query progress, average score, weakest dimension, and recommendations.
- `GET /actuator/health`
  - Health endpoint for deployment health checks (Render-ready).

---

## Scoring and Progression

### Rubric Dimensions

- Requirement understanding
- Logical clarity
- Technical feasibility
- Edge case coverage
- Communication structure

### Salary Tier Mapping

- Tier 1: `0-60` -> `Intern`
- Tier 2: `60-75` -> `Junior Engineer`
- Tier 3: `75-85` -> `Mid-Level`
- Tier 4: `85-95` -> `Senior`
- Tier 5: `95+` -> `Staff`

---

## Data Model

Key tables/entities:

- `users`
- `challenges`
- `submissions`
- `evaluations`

Relationship summary:

- `User -> Submission -> Evaluation`
- `Challenge -> Submission`

---

## Setup Guide

### Prerequisites

- Java `17+`
- Git

### Install Dependencies and Run Tests

```bash
./mvnw test
```

### Run the Application

```bash
./mvnw spring-boot:run
```

Default URL:

- [http://localhost:8080](http://localhost:8080)

### Database Profiles

- Default profile: in-memory `H2` (for local development)
- MySQL profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

- PostgreSQL profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Profile config files:

- `src/main/resources/application-mysql.yml`
- `src/main/resources/application-postgres.yml`

Actuator health check:

- `http://localhost:8080/actuator/health`

---

## Deployment

### Deployed on Render

This project is ready to deploy on Render using the repository `Dockerfile`.

- Runtime: `Docker`
- Public URL: Render assigns a public URL after deployment (for example `https://<service>.onrender.com`)
- Health check endpoint: `GET /actuator/health`

Recommended Render health check path:

- `/actuator/health`

### Profile Switching

Use Spring profiles to switch database/runtime configuration without changing code.

- Local default (H2): no profile
- MySQL: `SPRING_PROFILES_ACTIVE=mysql`
- PostgreSQL: `SPRING_PROFILES_ACTIVE=postgres`

Examples:

```bash
SPRING_PROFILES_ACTIVE=mysql java -jar target/ai-gamified-career-platform-0.0.1-SNAPSHOT.jar
```

```bash
SPRING_PROFILES_ACTIVE=postgres java -jar target/ai-gamified-career-platform-0.0.1-SNAPSHOT.jar
```

### External Config Support

The application supports externalized configuration through standard Spring Boot mechanisms:

- Environment variables (recommended on Render)
- `SPRING_APPLICATION_JSON`
- External config files via `SPRING_CONFIG_ADDITIONAL_LOCATION`

Common environment variables:

- `PORT` (used by the Docker startup command)
- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Example (Render + PostgreSQL):

```bash
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
```

Example (Render + MySQL):

```bash
SPRING_PROFILES_ACTIVE=mysql
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/<db>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
```

---

## Quick API Usage

### 1) Create User

```bash
curl -X POST http://localhost:8080/api/user \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo_user_1"}'
```

### 2) Generate Quest

```bash
curl -X POST http://localhost:8080/api/challenge/generate \
  -H 'Content-Type: application/json' \
  -d '{"difficulty":"INTERMEDIATE"}'
```

### 3) Submit Answer

```bash
curl -X POST http://localhost:8080/api/submission \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1,
    "challengeId": 1,
    "answer": "# Architecture\nDesign API + queue + worker + retry + observability ..."
  }'
```

### 4) Get Progress

```bash
curl http://localhost:8080/api/user/1/progress
```

---

## Future Improvements

- Replace heuristic evaluator with production LLM orchestrator (LangChain4j/OpenAI/Azure)
- Async evaluation queue for high throughput
- Full Vue 3 + Pinia + Axios frontend integration
- Leaderboard and advanced progression mechanics
- Multi-agent evaluation and bias calibration
