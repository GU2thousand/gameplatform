export interface TestAccount {
  username: string;
  email: string;
  password: string;
}

function compactSegment(value: string): string {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "")
    .slice(0, 14) || "user";
}

/**
 * Returns a collision-resistant account without relying on shared mutable state.
 * The values stay within conservative username/password validation limits.
 */
export function uniqueAccount(label = "user"): TestAccount {
  const timestamp = Date.now().toString(36);
  const entropy = Math.random().toString(36).slice(2, 10);
  const stem = `e2e_${compactSegment(label)}_${timestamp}_${entropy}`.slice(0, 48);

  return {
    username: stem,
    email: `${stem}@example.test`,
    password: `E2e!${timestamp}Aa9${entropy}`
  };
}

export const STRONG_FIRST_ANSWER = `
## Goal and assumptions
I would first confirm the target user, success metric, traffic assumptions, latency objective, and failure budget. The first release should optimize for a measurable user outcome while keeping scope reversible.

## Proposed solution
Use a versioned API behind an authenticated gateway. Persist the source of truth in a relational database, publish durable events through an outbox, and process background work with idempotent workers. Every write accepts an idempotency key and returns a stable resource identifier.

## Reliability and observability
Apply bounded retries with jitter, a dead-letter path, rate limits, request tracing, structured logs, and dashboards for latency, error rate, throughput, and queue age. Define an SLO and alerts before launch. Degrade optional features when a dependency is unavailable.

## Security and rollout
Enforce least privilege, validate input, encrypt sensitive data, retain only necessary records, and audit privileged actions. Release behind a feature flag, canary the change, compare metrics with a control group, and keep a tested rollback plan.

## Trade-offs
This design accepts asynchronous consistency for background work in exchange for resilience and predictable latency. I would revisit the architecture when measured scale or product requirements justify the added complexity.
`.trim();

export const IMPROVED_SECOND_ANSWER = `
## Outcomes
The primary outcome is successful task completion. Guardrails are p95 latency below 300 ms, error rate below 0.5%, no unauthorized data exposure, and support load within the current operational budget. I would validate assumptions with a one-week discovery phase.

## Interfaces and data
Expose POST /v1/jobs with an idempotency key, GET /v1/jobs/{id}, and DELETE /v1/jobs/{id}. Validate schemas at the boundary. Store jobs, ownership, status transitions, and idempotency records transactionally. Use an outbox event to hand work to partitioned consumers without a dual-write gap.

## Capacity and failure handling
Estimate peak request volume, payload size, storage growth, and worker service time before selecting capacity. Use timeouts, exponential backoff with jitter, circuit breakers, bounded queues, poison-message isolation, and replay tooling. Consumers are idempotent, so at-least-once delivery cannot duplicate user-visible effects.

## Security, privacy, and abuse
Authenticate every request, authorize against resource ownership, rotate secrets, encrypt data in transit and at rest, redact logs, set retention limits, and provide deletion. Add per-user quotas and anomaly alerts for abusive traffic.

## Delivery and measurement
Ship behind a feature flag to internal users, then 5%, 25%, and 100% cohorts with automated rollback thresholds. Instrument distributed traces and dashboards for RED metrics, saturation, queue age, business conversion, and cohort retention. Run failure injection and restore drills before full release.

## Alternatives
A synchronous design is simpler at low volume but couples user latency to downstream work. The queued design adds operational cost, so I would start synchronous if measurements show adequate headroom and introduce the outbox path only when needed.
`.trim();
