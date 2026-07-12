package com.gamingplatform.controller;

import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.dto.ChallengeGenerateRequest;
import com.gamingplatform.dto.ChallengeResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.service.ChallengeService;
import com.gamingplatform.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final CurrentUserService currentUserService;

    public ChallengeController(ChallengeService challengeService, CurrentUserService currentUserService) {
        this.challengeService = challengeService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/generate")
    public ChallengeResponse generate(@Valid @RequestBody(required = false) ChallengeGenerateRequest request) {
        Challenge challenge = challengeService.generate(toGenerationInput(request), currentUserService.requireUser());
        return toResponse(challenge);
    }

    @GetMapping({"", "/history"})
    public List<ChallengeResponse> history() {
        return challengeService.history(currentUserService.requireUserId()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ChallengeResponse get(@PathVariable Long id) {
        return toResponse(challengeService.getOwnedById(id, currentUserService.requireUserId()));
    }

    private ChallengeGenerationInput toGenerationInput(ChallengeGenerateRequest request) {
        if (request == null) {
            return new ChallengeGenerationInput(null, null, null, null, null, null, null, null);
        }

        return new ChallengeGenerationInput(
                request.getDifficulty(),
                request.getRoleTrack(),
                request.getChallengeType(),
                request.getFocusGoal(),
                request.getBusinessContext(),
                request.getCustomRequirements(),
                request.getCustomConstraints(),
                request.getCustomAcceptanceCriteria()
        );
    }

    private ChallengeResponse toResponse(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDifficulty(),
                challenge.getContext(),
                challenge.getRequirements(),
                challenge.getConstraints(),
                challenge.getAcceptanceCriteria(),
                challenge.getExpectedOutputFormat(),
                challenge.getRoleTrack(),
                challenge.getChallengeType(),
                challenge.getFocusGoal(),
                challenge.getGenerationProvider(),
                challenge.getCreatedAt()
        );
    }
}
