package com.gamingplatform.controller;

import com.gamingplatform.dto.ChallengeResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.security.CurrentUserService;
import com.gamingplatform.service.ChallengeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeHistoryController {
    private final ChallengeService challenges;
    private final CurrentUserService currentUser;

    public ChallengeHistoryController(ChallengeService challenges, CurrentUserService currentUser) {
        this.challenges = challenges;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<ChallengeResponse> history() {
        return challenges.history(currentUser.requireUserId()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ChallengeResponse get(@PathVariable Long id) {
        return toResponse(challenges.getOwnedById(id, currentUser.requireUserId()));
    }

    private ChallengeResponse toResponse(Challenge challenge) {
        return new ChallengeResponse(challenge.getId(), challenge.getTitle(), challenge.getDifficulty(),
                challenge.getContext(), challenge.getRequirements(), challenge.getConstraints(),
                challenge.getAcceptanceCriteria(), challenge.getExpectedOutputFormat(), challenge.getRoleTrack(),
                challenge.getChallengeType(), challenge.getFocusGoal(), challenge.getGenerationProvider(),
                challenge.getCreatedAt());
    }
}
