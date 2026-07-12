# Career Quest — Architecture and Operations

## Product

Career Quest is a full-stack practice loop for realistic product and engineering work. A user creates a PRD, system-design, or API-design challenge; submits multiple answers; receives explainable rubric feedback; compares attempts; and continues with a challenge targeted at the weakest skill.

The application is usable without a paid model. Local mode provides deterministic challenge templates and heuristic evaluation. LangChain4j mode calls the configured OpenAI-compatible service and falls back to the local evaluator when the primary call fails.

## Architecture

Browser UI

→ Spring Security Session and CSRF boundary

→ REST controllers with ownership checks

→ challenge, submission, progress, account, and recommendation services

→ local or LangChain4j AI clients

→ JPA repositories

→ Flyway-managed H2, PostgreSQL, or MySQL

### Main components

- SecurityConfig: Session authentication, BCrypt, CSRF cookies, JSON security errors, logout, and resource protection.
- ChallengeService: custom challenge generation, AI fallback, ownership, provider attribution, and daily quota consumption.
- SubmissionService: persisted asynchronous jobs, idempotency, history, and comparison.
- SubmissionProcessor: claim/evaluate/complete state machine with transactional evaluation, XP, and status persistence.
- SubmissionRecoveryService: startup and scheduled recovery of pending and stale processing jobs.
- EvaluationEngine: validates AI output, applies challenge-specific rubric weights, and records evidence and provider.
- ProgressService: distinct completed challenges, total attempts, score averages, streaks, seven-day activity, trends, and plan.
- AccountService: credential-free export and ownership-safe cascade deletion, including shared legacy challenges.
- RateLimitInterceptor and AiQuotaService: per-principal endpoint limits, daily AI quota, and Micrometer counters/timers.

## Security model

Credentials are accepted only by register, login, and the optional migration claim endpoint. Passwords are stored as BCrypt hashes and never returned.

Authentication is server-side Session based. Every mutation, including register and login, requires a CSRF token from GET /api/auth/csrf. Authentication rotates the Session ID and CSRF token. The browser therefore requests a fresh token before every mutation.

Controllers derive identity from the authenticated Session. A compatibility userId field may still be deserialized in older payloads, but it never controls ownership.

Cross-account resources return 404 to avoid confirming that another user owns the identifier. Authentication and CSRF errors use consistent JSON bodies.

Production HTTPS deployments must set SESSION_COOKIE_SECURE=true.

## Submission state machine

POST /api/submissions returns HTTP 202 with a stable submission identifier.

PENDING → PROCESSING → COMPLETED

PENDING → PROCESSING → FAILED

The AI call happens outside the write transaction. Evaluation persistence, incremental XP, and COMPLETED status happen in one transaction. A failure is stored as FAILED with a bounded error message.

The unique user + idempotency key constraint prevents duplicate jobs. Reusing a key with the same challenge and answer returns the existing job. Reusing it with a different payload returns HTTP 409. Concurrent retries therefore cannot duplicate XP.

On startup and on a schedule, persisted PENDING jobs are resubmitted and stale PROCESSING jobs are returned to PENDING.

## Scoring

The five dimensions are:

- requirement understanding
- logical clarity
- technical feasibility
- edge-case coverage
- communication structure

The default weights are 25%, 20%, 25%, 15%, and 15%. Product/PRD, system-design, and API-design challenges override those weights to emphasize the dimensions that matter for that task.

Each completed evaluation stores:

- scores and applied weights
- final score and skill tier
- overall feedback
- strengths and priority improvements
- example answer outline
- next improvement track
- local, langchain4j, fallback, or legacy provider

The first completed attempt awards rounded score XP. Later attempts on the same challenge award only a positive improvement over the previous best.

## Persistence and migrations

Hibernate uses ddl-auto=validate. Flyway owns schema creation and upgrades.

- db/migration/h2
- db/migration/postgresql
- db/migration/mysql

V1 represents the legacy schema. V2 adds authentication, ownership, asynchronous state, idempotency, explainable evaluation data, long-text storage, and query indexes.

Fresh databases run V1 and V2. Existing legacy schemas must be backed up and inspected before their one-time baseline migration:

~~~bash
FLYWAY_BASELINE_ON_MIGRATE=true ./mvnw spring-boot:run
~~~

After the successful upgrade, remove the flag. The safe default is false.

Legacy accounts have no verifiable password. During a controlled migration window, an operator may configure a high-entropy APP_LEGACY_CLAIM_SECRET and call POST /api/auth/claim-legacy. Clear the secret after migration.

Challenges used by exactly one legacy user are assigned to that user. Shared legacy challenges keep no owner and are accessible only to users with an existing submission. Deleting one participant preserves the other participant’s history.

## Configuration

Local H2 and local AI:

~~~bash
./mvnw spring-boot:run
~~~

PostgreSQL:

~~~bash
SPRING_PROFILES_ACTIVE=postgres \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/gamingplatform \
SPRING_DATASOURCE_USERNAME=gamingplatform \
SPRING_DATASOURCE_PASSWORD=gamingplatform \
./mvnw spring-boot:run
~~~

LangChain4j:

~~~bash
APP_AI_PROVIDER=langchain4j \
OPENAI_API_KEY=<key> \
OPENAI_MODEL=gpt-4o-mini \
./mvnw spring-boot:run
~~~

Operational variables:

- APP_REQUESTS_PER_MINUTE
- APP_AI_REQUESTS_PER_DAY
- APP_SUBMISSION_RECOVERY_INTERVAL_MS
- SESSION_COOKIE_SECURE
- MANAGEMENT_ENDPOINTS
- APP_LEGACY_CLAIM_SECRET
- FLYWAY_BASELINE_ON_MIGRATE
- APP_DEBUG_ENABLED
- H2_CONSOLE_ENABLED

The default management exposure is health and info. Add metrics only when the deployment has an appropriate monitoring access boundary.

## API

Authentication:

- GET /api/auth/csrf
- POST /api/auth/register
- POST /api/auth/login
- GET /api/auth/me
- POST /api/auth/logout
- POST /api/auth/claim-legacy

Challenges:

- POST /api/challenge/generate
- GET /api/challenges
- GET /api/challenges/{id}
- POST /api/training/next

Submissions:

- POST /api/submissions
- GET /api/submissions/{id}
- GET /api/submissions
- GET /api/challenges/{challengeId}/attempts
- GET /api/attempts/compare

Progress and account:

- GET /api/user/me/progress
- GET /api/account/export
- DELETE /api/account
- GET /actuator/health

## Verification

Fast suite:

~~~bash
./mvnw clean verify
~~~

PostgreSQL and MySQL migration suite:

~~~bash
./mvnw clean verify -Pcontainer-tests
~~~

Browser suite:

~~~bash
cd qa/e2e
npm ci
npm run install:browsers
E2E_BASE_URL=http://127.0.0.1:18080 npm test
~~~

Current evidence:

- 29 Surefire tests
- 2 Failsafe/Testcontainers tests covering PostgreSQL 16 and MySQL 8.4, fresh and legacy paths
- 3 Playwright journeys
- Axe serious/critical violation count: zero
- 390px horizontal overflow: zero

CI runs the container profile, browser type-check, Chromium Playwright/Axe suite, and production Docker image build. Browser failures retain logs, screenshots, video, traces, Axe output, and overflow diagnostics.

## Deployment

The Docker image is multi-stage and runs as UID 10001. Docker Compose provides PostgreSQL and the application.

For an internet deployment:

- use PostgreSQL or MySQL, not in-memory H2
- terminate TLS and enable Secure cookies
- keep debug and H2 Console disabled
- use a persistent Session strategy when scaling beyond one application instance
- replace in-memory rate-limit and quota counters with shared infrastructure for multiple instances
- define retention, backup deletion, incident response, and AI-provider privacy terms
- monitor health, latency, AI fallback/failure, queue age, submission outcomes, and rate-limit events
