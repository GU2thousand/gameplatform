package com.gamingplatform.controller;

import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.dto.ChallengeGenerateRequest;
import com.gamingplatform.dto.ChallengeResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.service.ChallengeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping("/generate")
    public ChallengeResponse generate(@RequestBody(required = false) ChallengeGenerateRequest request) {
        Challenge challenge = challengeService.generate(toGenerationInput(request));
        return toResponse(challenge);
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
                challenge.getCreatedAt()
        );
    }
}
