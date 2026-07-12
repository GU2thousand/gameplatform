# Career Quest — AI-Powered Career Skills Training Platform

Career Quest provides realistic PRD, system design, and API design exercises for product managers, software engineers, and cross-functional professionals. Rather than serving as a one-off scoring demo, it delivers a complete training loop:

Generate a challenge → save a draft → receive an asynchronous evaluation → review evidence and scoring weights → retry the challenge → compare progress → continue training based on identified weaknesses.

## Implemented Capabilities

### Accounts, Security, and Privacy

- BCrypt password hashing, server-side session authentication, logout, and session restoration after refresh
- CSRF protection for all write operations; successful login rotates both the session ID and CSRF token
- All business resources are authorized against the currently authenticated user; client-supplied `userId` values are never trusted
- Consistent JSON responses for 401, 403, 404, 409, 429, and validation errors
- Full account data export and deletion of the account with its associated training data
- Secure one-time claiming of legacy passwordless accounts when an operations secret is enabled
- CSP, frame-embedding protection, MIME sniffing protection, Referrer Policy, and Permissions Policy

### Complete Training Loop

- Three quick-start templates: product management, system design, and API design
- Customizable difficulty, role, training focus, business context, requirements, constraints, and acceptance criteria
- Restoration of the form, answer, challenge, and evaluation within the current browser tab after a refresh
- Multiple attempts for the same challenge, challenge history, attempt history, and comparison between any two results
- Separate tracking of completed challenges and total attempts
- Current and longest training streaks, activity over the last seven days, score trends, and a seven-day training plan
- Automatic generation of the next challenge based on the learner's weakest average rubric dimension

### Transparent Evaluation

- Five rubric dimensions: requirements understanding, logical clarity, technical feasibility, edge-case coverage, and communication structure
- Different scoring weights for PRD, system design, and API design challenges
- Total score, per-dimension scores, per-dimension weights, strengths, improvement areas, reference outline, and next steps
- Explicit display of the evaluation source: `local`, `langchain4j`, or `fallback`
- XP is awarded only for improvement over the previous best score on the same challenge, preventing repetitive score farming

### Asynchronous Processing and Reliability

- `POST` submissions return 202 immediately; the frontend polls `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` states
- Uniqueness is enforced by `user + Idempotency-Key`; identical requests return the same job without duplicate evaluation or XP
- AI calls run outside database transactions; evaluation, XP, and completion status are committed atomically in a single transaction
- Failure reasons are persisted; startup and scheduled recovery jobs resume unprocessed or timed-out submissions
- Per-user/IP request rate limits, daily AI quotas, and Micrometer counters and timing metrics

### Databases and Migrations

- H2, PostgreSQL 16, and MySQL 8.4
- Flyway V1 baseline and V2 upgrade migrations; schema changes no longer depend on automatic Hibernate DDL updates
- PostgreSQL `TEXT`, MySQL `LONGTEXT`, and H2 `CLOB` support for long evaluation content
- Safe backfilling of challenge ownership, evaluation source, weights, status, and idempotency fields for legacy data
- Indexes for common history queries, status recovery, foreign-key collections, and ownership lookups
- Formal Testcontainers coverage for both fresh and legacy PostgreSQL/MySQL migration paths

### UI and Accessibility

- Complete Chinese/English interface switching with persisted language preference
- Desktop workspace with a sticky summary panel and automatic single-column layout on mobile
- Clear four-step training stepper, presets, progress dashboard, history, and comparison areas
- Keyboard navigation, skip links, status announcements, error alerts, native dialogs, and reduced-motion support
- Offline status, draft protection, challenge copying, feedback downloads, data export, and deletion confirmation
- Server-provided content is never rendered with `innerHTML`

## Technology Stack

- Java 17
- Spring Boot 3.5.16
- Spring Web, Security, Data JPA, Validation, and Actuator
- Flyway
- LangChain4j 1.17.2
- H2, PostgreSQL, and MySQL
- Vanilla HTML, CSS, and JavaScript
- Maven Wrapper, Docker, and Docker Compose
- Playwright, Axe, and Testcontainers

## Quick Start

Requirement: Java 17 or later.

~~~bash
./mvnw clean verify
./mvnw spring-boot:run
~~~

Open http://localhost:8080/.

The default configuration uses an in-memory H2 database and local template/heuristic evaluation, so no OpenAI API key is required. H2 is suitable for development and demonstrations; use PostgreSQL or MySQL when accounts and training records must persist across restarts.

### Full PostgreSQL Stack

~~~bash
cp .env.example .env
docker compose up --build
~~~

The application is available at http://localhost:8080/, and PostgreSQL is exposed to the host on port 5433 by default.

### LangChain4j + OpenAI

~~~bash
APP_AI_PROVIDER=langchain4j \
OPENAI_API_KEY=<your-key> \
OPENAI_MODEL=gpt-4o-mini \
./mvnw spring-boot:run
~~~

If the primary AI call fails, the application uses the local fallback and clearly marks the evaluation source in the result. When remote AI is enabled, challenge content and answers are sent to the configured model service; update the privacy notice to match your deployment context.

## Formal QA

### Backend and Fast Migration Tests

~~~bash
./mvnw clean verify
~~~

The current suite includes 29 H2/service/API integration tests covering authentication, CSRF, authorization, input boundaries, asynchronous success and failure, concurrent idempotency, XP, account export and deletion, legacy account claiming, shared legacy challenges, AI fallback, and fresh/legacy H2 migrations.

### PostgreSQL/MySQL Container Migration Tests

Local Docker is required:

~~~bash
./mvnw clean verify -Pcontainer-tests
~~~

This command starts PostgreSQL 16 and MySQL 8.4 containers on random ports and verifies:

- Fresh V1 + V2 migration
- Legacy baseline 1 → V2 migration
- Flyway history, validation, and repeated migration with zero pending changes
- Hibernate `ddl-auto=validate`
- Unicode long text, preservation of legacy data, and ownership of single-user and shared challenges
- Indexes, foreign keys, and unique constraints

The default `verify` command does not start containers. CI explicitly enables `container-tests`, so a missing Docker environment cannot produce a false pass.

### Browser, Mobile, and Accessibility Tests

Start the application on any port, then run:

~~~bash
cd qa/e2e
npm ci
npm run install:browsers
E2E_BASE_URL=http://127.0.0.1:18080 npm test
~~~

The three formal user journeys cover:

- Registration, logout, login, and session restoration after refresh
- Preset generation, draft restoration, two asynchronous evaluations, history, progress, and comparison
- Chinese/English switching, short-answer validation, 503 feedback, horizontal overflow at 390px, and Axe scanning

On failure, the suite preserves screenshots, video, traces, Axe JSON, and mobile overflow diagnostics.

## Primary APIs

### Authentication

- `GET /api/auth/csrf`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `POST /api/auth/claim-legacy`

### Training

- `POST /api/challenge/generate`
- `GET /api/challenges`
- `GET /api/challenges/{id}`
- `POST /api/training/next`

### Asynchronous Evaluation and History

- `POST /api/submissions`
- `GET /api/submissions/{id}`
- `GET /api/submissions`
- `GET /api/challenges/{challengeId}/attempts`
- `GET /api/attempts/compare?firstId=...&secondId=...`

### Progress and Account

- `GET /api/user/me/progress`
- `GET /api/account/export`
- `DELETE /api/account`
- `GET /actuator/health`

The browser client retrieves a CSRF token before every write request and sends it in the `X-XSRF-TOKEN` header. Never store passwords, session IDs, or tokens in `localStorage`.

## Key Configuration

See `.env.example` for the complete template.

- `APP_AI_PROVIDER`: `local` or `langchain4j`
- `APP_REQUESTS_PER_MINUTE`: per-endpoint request allowance per minute
- `APP_AI_REQUESTS_PER_DAY`: daily AI allowance per user
- `APP_SUBMISSION_RECOVERY_INTERVAL_MS`: asynchronous job recovery interval
- `SESSION_COOKIE_SECURE`: must be `true` in HTTPS production environments
- `MANAGEMENT_ENDPOINTS`: defaults to `health,info`; explicitly add `metrics` as required by your monitoring system
- `APP_LEGACY_CLAIM_SECRET`: set a high-entropy secret only temporarily during the legacy-account migration window, then clear it
- `FLYWAY_BASELINE_ON_MIGRATE`: defaults to `false`

Before the first upgrade of an existing legacy Hibernate schema, back up the database and verify its structure. Set `FLYWAY_BASELINE_ON_MIGRATE=true` only temporarily for the initial migration, then restore it to `false`. Do not enable it for a new database.

## Deployment Notes

- Use PostgreSQL or MySQL in production; do not use in-memory H2
- Serve the application only over HTTPS and set `SESSION_COOKIE_SECURE=true`
- AI processing, rate-limit state, and quota state are currently single-instance implementations; multi-instance deployments should use a shared queue, shared rate-limit/quota storage, and distributed sessions
- Define explicit retention and deletion periods for databases, user export files, and backups
- The H2 Console and AI debug endpoints are disabled by default; enable them only temporarily in a trusted local environment
- See `AI_Gamified_Career_Training_Platform_README.md` for detailed architecture and operations guidance
- See `PRIVACY.md` for data-processing information

## Current Validation Results

- Surefire: 29 tests, 0 failures
- Failsafe/Testcontainers: 2 tests, 0 failures
- Backend and database total: 31 tests, 0 failures
- Playwright/Axe: all 3 real-browser journeys passed
- 390px mobile viewport: no horizontal overflow
- Axe: no serious or critical violations
- PostgreSQL 16 and MySQL 8.4: fresh/legacy Flyway migrations and Hibernate validation passed
- Production image: built successfully; in a standalone container, the health endpoint reported `UP`, the homepage returned 200, and the application ran as non-root UID 10001
