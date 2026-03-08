package com.gamingplatform.ai.impl;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.entity.Difficulty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "local", matchIfMissing = true)
public class TemplateChallengeAiClient implements ChallengeAiClient {

    private final AtomicInteger counter = new AtomicInteger(0);

    private static final Map<Difficulty, List<GeneratedChallenge>> BANK = Map.of(
            Difficulty.BEGINNER,
            List.of(
                    new GeneratedChallenge(
                            "Design a Basic Notification Retry Policy",
                            Difficulty.BEGINNER,
                            "You are collaborating with a PM to improve push notification delivery.",
                            List.of(
                                    "Define API contract for notification submit endpoint",
                                    "Explain retry strategy for transient failures",
                                    "Specify minimal metrics for monitoring"
                            ),
                            List.of(
                                    "Must support 5k requests per minute",
                                    "Use idempotency key to avoid duplicates",
                                    "No external queue service in phase 1"
                            ),
                            List.of(
                                    "API request/response examples",
                                    "Retry policy and backoff logic",
                                    "Failure handling section"
                            ),
                            "Markdown"
                    )
            ),
            Difficulty.INTERMEDIATE,
            List.of(
                    new GeneratedChallenge(
                            "Design a Scalable Notification System",
                            Difficulty.INTERMEDIATE,
                            "A gaming platform needs reliable event notifications for achievements and ranking updates.",
                            List.of(
                                    "Provide high-level architecture and core components",
                                    "Define data model for notification jobs and delivery state",
                                    "Specify API endpoints for send and status query"
                            ),
                            List.of(
                                    "Peak 50k notification events per minute",
                                    "At-least-once delivery is acceptable",
                                    "Latency target under 2 seconds for 95th percentile"
                            ),
                            List.of(
                                    "Architecture diagram description",
                                    "API specification with error codes",
                                    "Tradeoff analysis section"
                            ),
                            "Markdown"
                    ),
                    new GeneratedChallenge(
                            "Design Matchmaking Session APIs",
                            Difficulty.INTERMEDIATE,
                            "The platform introduces ranked queue and needs robust session APIs.",
                            List.of(
                                    "Draft endpoints to create/cancel queue requests",
                                    "Describe consistency model for match assignment",
                                    "Include anti-abuse and fairness checks"
                            ),
                            List.of(
                                    "Support multi-region deployment",
                                    "Prevent duplicate queue join requests",
                                    "No synchronous cross-region transactions"
                            ),
                            List.of(
                                    "Clear API schemas",
                                    "Edge case handling",
                                    "Scalability considerations"
                            ),
                            "Markdown"
                    )
            ),
            Difficulty.ADVANCED,
            List.of(
                    new GeneratedChallenge(
                            "Design Cross-Region Real-Time Event Pipeline",
                            Difficulty.ADVANCED,
                            "Leadership asks for a globally distributed event system for game telemetry and user alerts.",
                            List.of(
                                    "Propose cross-region event ingestion architecture",
                                    "Define disaster recovery and replay strategy",
                                    "Describe observability and SLO management"
                            ),
                            List.of(
                                    "RPO <= 5 minutes and RTO <= 15 minutes",
                                    "Must isolate noisy tenants",
                                    "Data retention policy is 30 days"
                            ),
                            List.of(
                                    "Multi-region architecture explanation",
                                    "Detailed failure scenarios and mitigations",
                                    "Capacity planning assumptions"
                            ),
                            "Markdown"
                    )
            )
    );

    @Override
    public GeneratedChallenge generate(Difficulty difficulty) {
        List<GeneratedChallenge> candidates = BANK.getOrDefault(difficulty, BANK.get(Difficulty.INTERMEDIATE));
        int index = Math.floorMod(counter.getAndIncrement(), candidates.size());
        return candidates.get(index);
    }
}
