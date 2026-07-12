package com.gamingplatform.ai.impl;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.entity.Difficulty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TemplateChallengeAiClient implements ChallengeAiClient {

    private static final int MAX_TITLE_LENGTH = 255;

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

        boolean hasCustomBusinessContext = hasText(input.businessContext());
        List<String> baseRequirements = hasCustomBusinessContext
                ? contextNeutralRequirements(difficulty)
                : base.requirements();
        List<String> baseConstraints = hasCustomBusinessContext
                ? contextNeutralConstraints(difficulty)
                : base.constraints();
        List<String> baseAcceptanceCriteria = hasCustomBusinessContext
                ? contextNeutralAcceptanceCriteria()
                : base.acceptanceCriteria();

        return new GeneratedChallenge(
                buildTitle(base, input),
                difficulty,
                buildContext(base.context(), input),
                mergeSection(baseRequirements, derivedRequirements(input), input.customRequirementsOrEmpty()),
                mergeSection(baseConstraints, derivedConstraints(input), input.customConstraintsOrEmpty()),
                mergeSection(baseAcceptanceCriteria, derivedAcceptanceCriteria(input), input.customAcceptanceCriteriaOrEmpty()),
                base.expectedOutputFormat()
        );
    }

    private List<String> contextNeutralRequirements(Difficulty difficulty) {
        List<String> requirements = new ArrayList<>(List.of(
                "Translate the business problem into explicit functional and non-functional requirements.",
                "Propose an implementation approach with clear component or API responsibilities.",
                "Define rollout, monitoring, and failure-handling plans with measurable outcomes."
        ));
        if (difficulty == Difficulty.ADVANCED) {
            requirements.add("Explain scaling, resilience, and cross-team operational ownership tradeoffs.");
        }
        return requirements;
    }

    private List<String> contextNeutralConstraints(Difficulty difficulty) {
        List<String> constraints = new ArrayList<>(List.of(
                "State assumptions explicitly where the business context does not provide exact numbers.",
                "Preserve security, data integrity, and service reliability during rollout.",
                "Use measurable latency, capacity, quality, or cost targets where relevant."
        ));
        if (difficulty == Difficulty.BEGINNER) {
            constraints.add("Keep the initial solution small enough to deliver incrementally.");
        }
        return constraints;
    }

    private List<String> contextNeutralAcceptanceCriteria() {
        return List.of(
                "The proposal traces each recommendation to a stated requirement.",
                "The response explains key tradeoffs and at least two realistic failure scenarios.",
                "Success metrics and a safe validation or rollout plan are clearly defined."
        );
    }

    private String buildTitle(GeneratedChallenge base, ChallengeGenerationInput input) {
        String challengeType = normalize(input.challengeType());
        String focusGoal = normalize(input.focusGoal());
        String businessContext = normalize(input.businessContext());

        String title;
        if (hasText(challengeType) && hasText(businessContext)) {
            title = challengeType + ": " + summarize(businessContext);
        } else if (hasText(challengeType) && hasText(focusGoal)) {
            title = challengeType + ": " + focusGoal;
        } else if (hasText(challengeType)) {
            title = "Custom " + challengeType + " Challenge";
        } else if (hasText(focusGoal)) {
            title = base.title() + " - " + focusGoal;
        } else {
            title = base.title();
        }
        return truncate(title, MAX_TITLE_LENGTH);
    }

    private String buildContext(String baseContext, ChallengeGenerationInput input) {
        List<String> sections = new ArrayList<>();

        if (hasText(input.businessContext())) {
            sections.add("User-specified business context: " + input.businessContext().trim());
        } else {
            sections.add(baseContext);
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
        return String.join(" ", sections);
    }

    private List<String> derivedRequirements(ChallengeGenerationInput input) {
        List<String> derived = new ArrayList<>();
        if (hasText(input.roleTrack())) {
            derived.add("Tailor the response to the " + input.roleTrack().trim() + " interview track.");
        }
        if (hasText(input.challengeType())) {
            derived.add("Use this exercise format for the deliverable: " + input.challengeType().trim() + ".");
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
        return truncate(normalized, 72);
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
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
