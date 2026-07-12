package com.gamingplatform.controller;

import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.dto.ChallengeResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.security.CurrentUserService;
import com.gamingplatform.service.ChallengeService;
import com.gamingplatform.service.ProgressService;
import com.gamingplatform.service.RecommendationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training")
public class TrainingController {
    private final CurrentUserService currentUser;
    private final ProgressService progress;
    private final ChallengeService challenges;
    private final RecommendationService recommendations;

    public TrainingController(CurrentUserService currentUser, ProgressService progress,
                              ChallengeService challenges, RecommendationService recommendations) {
        this.currentUser = currentUser;
        this.progress = progress;
        this.challenges = challenges;
        this.recommendations = recommendations;
    }

    @PostMapping("/next")
    public ChallengeResponse next() {
        UserProfile user = currentUser.requireUser();
        RubricDimension weakest = progress.weakestDimension(user.getId());
        String focus = weakest == null ? "Complete a baseline diagnostic"
                : "Improve " + recommendations.dimensionLabel(weakest);
        String type = switch (weakest == null ? RubricDimension.REQUIREMENT_UNDERSTANDING : weakest) {
            case REQUIREMENT_UNDERSTANDING, COMMUNICATION_STRUCTURE -> "PRD";
            case TECHNICAL_FEASIBILITY, EDGE_CASE_COVERAGE -> "System Design";
            case LOGICAL_CLARITY -> "API Design";
        };
        Challenge challenge = challenges.generate(new ChallengeGenerationInput(null, "Adaptive Training", type,
                focus, null, List.of(), List.of(), List.of()), user);
        return new ChallengeResponse(challenge.getId(), challenge.getTitle(), challenge.getDifficulty(),
                challenge.getContext(), challenge.getRequirements(), challenge.getConstraints(),
                challenge.getAcceptanceCriteria(), challenge.getExpectedOutputFormat(), challenge.getRoleTrack(),
                challenge.getChallengeType(), challenge.getFocusGoal(), challenge.getGenerationProvider(),
                challenge.getCreatedAt());
    }
}
