package com.gamingplatform.controller;

import com.gamingplatform.dto.ChallengeGenerateRequest;
import com.gamingplatform.dto.ChallengeResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.Difficulty;
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
        Difficulty difficulty = request == null ? Difficulty.INTERMEDIATE : request.getDifficulty();
        Challenge challenge = challengeService.generate(difficulty);
        return toResponse(challenge);
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
