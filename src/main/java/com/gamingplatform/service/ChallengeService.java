package com.gamingplatform.service;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.Difficulty;
import com.gamingplatform.exception.InvalidAiOutputException;
import com.gamingplatform.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeAiClient challengeAiClient;
    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeAiClient challengeAiClient, ChallengeRepository challengeRepository) {
        this.challengeAiClient = challengeAiClient;
        this.challengeRepository = challengeRepository;
    }

    @Transactional
    public Challenge generate(Difficulty difficulty) {
        Difficulty resolvedDifficulty = difficulty == null ? Difficulty.INTERMEDIATE : difficulty;
        GeneratedChallenge generated = challengeAiClient.generate(resolvedDifficulty);
        validateGeneratedChallenge(generated);

        Challenge challenge = new Challenge();
        challenge.setTitle(generated.title());
        challenge.setDifficulty(generated.difficulty());
        challenge.setContext(generated.context());
        challenge.setRequirements(generated.requirements());
        challenge.setConstraints(generated.constraints());
        challenge.setAcceptanceCriteria(generated.acceptanceCriteria());
        challenge.setExpectedOutputFormat(generated.expectedOutputFormat());

        return challengeRepository.save(challenge);
    }

    @Transactional(readOnly = true)
    public Challenge getById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new com.gamingplatform.exception.NotFoundException("Challenge not found: " + challengeId));
    }

    private void validateGeneratedChallenge(GeneratedChallenge generated) {
        if (generated == null) {
            throw new InvalidAiOutputException("Challenge generation returned null payload");
        }
        if (isBlank(generated.title()) || isBlank(generated.context()) || isBlank(generated.expectedOutputFormat())) {
            throw new InvalidAiOutputException("Challenge payload missing required text fields");
        }
        validateList(generated.requirements(), "requirements");
        validateList(generated.constraints(), "constraints");
        validateList(generated.acceptanceCriteria(), "acceptanceCriteria");
    }

    private void validateList(List<String> values, String fieldName) {
        if (values == null || values.isEmpty() || values.stream().anyMatch(this::isBlank)) {
            throw new InvalidAiOutputException("Challenge payload has invalid " + fieldName);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
