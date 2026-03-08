package com.gamingplatform.ai.impl;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.entity.Difficulty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    public GeneratedChallenge generate(ChallengeGenerationInput input) {
        Difficulty difficulty = input == null ? Difficulty.INTERMEDIATE : input.resolvedDifficulty();
        List<GeneratedChallenge> candidates = BANK.getOrDefault(difficulty, BANK.get(Difficulty.INTERMEDIATE));
        int index = Math.floorMod(counter.getAndIncrement(), candidates.size());
        GeneratedChallenge base = candidates.get(index);

        if (input == null || !input.hasCustomPrompt()) {
            return base;
        }

        return new GeneratedChallenge(
                buildTitle(base, input),
                difficulty,
                buildContext(base.context(), input),
                mergeSection(base.requirements(), derivedRequirements(input), input.customRequirementsOrEmpty()),
                mergeSection(base.constraints(), derivedConstraints(input), input.customConstraintsOrEmpty()),
                mergeSection(base.acceptanceCriteria(), derivedAcceptanceCriteria(input), input.customAcceptanceCriteriaOrEmpty()),
                base.expectedOutputFormat()
        );
    }

    private String buildTitle(GeneratedChallenge base, ChallengeGenerationInput input) {
        String challengeType = normalize(input.challengeType());
        String focusGoal = normalize(input.focusGoal());
        String businessContext = normalize(input.businessContext());

        if (hasText(challengeType) && hasText(businessContext)) {
            return challengeType + ": " + summarize(businessContext);
        }
        if (hasText(challengeType) && hasText(focusGoal)) {
            return challengeType + ": " + focusGoal;
        }
        if (hasText(challengeType)) {
            return "Custom " + challengeType + " Challenge";
        }
        if (hasText(focusGoal)) {
            return base.title() + " - " + focusGoal;
        }
        return base.title();
    }

    private String buildContext(String baseContext, ChallengeGenerationInput input) {
        List<String> sections = new ArrayList<>();

        if (hasText(input.businessContext())) {
            sections.add("User-specified business context: " + input.businessContext().trim());
        }

        List<String> setup = new ArrayList<>();
        if (hasText(input.roleTrack())) {
            setup.add("track=" + input.roleTrack().trim());
        }
        if (hasText(input.challengeType())) {
            setup.add("type=" + input.challengeType().trim());
        }
        if (hasText(input.focusGoal())) {
            setup.add("focus=" + input.focusGoal().trim());
        }
        if (!setup.isEmpty()) {
            sections.add("Requested setup: " + String.join(", ", setup) + ".");
        }

        sections.add(baseContext);
        return String.join(" ", sections);
    }

    private List<String> derivedRequirements(ChallengeGenerationInput input) {
        List<String> derived = new ArrayList<>();
        if (hasText(input.roleTrack())) {
            derived.add("Tailor the response to the " + input.roleTrack().trim() + " interview track.");
        }
        if (hasText(input.challengeType())) {
            derived.add("Structure the deliverable as a " + input.challengeType().trim() + " exercise.");
        }
        if (hasText(input.focusGoal())) {
            derived.add("Explicitly address this requested focus: " + input.focusGoal().trim());
        }
        return derived;
    }

    private List<String> derivedConstraints(ChallengeGenerationInput input) {
        List<String> derived = new ArrayList<>();
        if (hasText(input.businessContext())) {
            derived.add("Keep the proposal grounded in this business context: " + summarize(input.businessContext()));
        }
        return derived;
    }

    private List<String> derivedAcceptanceCriteria(ChallengeGenerationInput input) {
        List<String> derived = new ArrayList<>();
        if (hasText(input.focusGoal())) {
            derived.add("The final answer clearly covers the requested focus area.");
        }
        if (!input.customRequirementsOrEmpty().isEmpty()) {
            derived.add("The solution traces back to the user-provided custom requirements.");
        }
        return derived;
    }

    private List<String> mergeSection(List<String> base, List<String> derived, List<String> custom) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        addAll(merged, base);
        addAll(merged, derived);
        addAll(merged, custom);
        return new ArrayList<>(merged);
    }

    private void addAll(LinkedHashSet<String> target, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            String normalized = normalize(value);
            if (normalized != null) {
                target.add(normalized);
            }
        }
    }

    private String summarize(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "";
        }
        return normalized.length() <= 72 ? normalized : normalized.substring(0, 69) + "...";
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean hasText(String value) {
        return normalize(value) != null;
    }
}
