package com.gamingplatform.service;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.exception.InvalidAiOutputException;
import com.gamingplatform.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeAiClient challengeAiClient;
    private final ChallengeRepository challengeRepository;
    private final UserService users;

    public ChallengeService(ChallengeAiClient challengeAiClient, ChallengeRepository challengeRepository, UserService users) {
        this.challengeAiClient = challengeAiClient;
        this.challengeRepository = challengeRepository;
        this.users = users;
    }

    @Transactional
    public com.gamingplatform.dto.ChallengeResponse generate(ChallengeGenerationInput input, Long userId) {
        GeneratedChallenge generated = challengeAiClient.generate(input);
        validateGeneratedChallenge(generated);

        Challenge challenge = new Challenge();
        challenge.setOwner(users.getById(userId));
        challenge.setTitle(generated.title());
        challenge.setDifficulty(generated.difficulty());
        challenge.setContext(generated.context());
        challenge.setRequirements(generated.requirements());
        challenge.setConstraints(generated.constraints());
        challenge.setAcceptanceCriteria(generated.acceptanceCriteria());
        challenge.setExpectedOutputFormat(generated.expectedOutputFormat());

        return response(challengeRepository.save(challenge));
    }

    @Transactional(readOnly = true)
    public Challenge getOwned(Long challengeId, Long userId) {
        return challengeRepository.findByIdAndOwner_Id(challengeId, userId)
                .orElseThrow(() -> new com.gamingplatform.exception.NotFoundException("Challenge not found: " + challengeId));
    }

    public static com.gamingplatform.dto.ChallengeResponse response(Challenge challenge) {
        return new com.gamingplatform.dto.ChallengeResponse(challenge.getId(), challenge.getTitle(), challenge.getDifficulty(),
                challenge.getContext(), List.copyOf(challenge.getRequirements()), List.copyOf(challenge.getConstraints()),
                List.copyOf(challenge.getAcceptanceCriteria()), challenge.getExpectedOutputFormat(), challenge.getCreatedAt());
    }

    private void validateGeneratedChallenge(GeneratedChallenge generated) {
        if (generated == null) {
            throw new InvalidAiOutputException("Challenge generation returned null payload");
        }
        if (isBlank(generated.title()) || isBlank(generated.context()) || isBlank(generated.expectedOutputFormat())) {
            throw new InvalidAiOutputException("Challenge payload missing required text fields");
        }
        if (generated.difficulty() == null || generated.title().length() > 255 || generated.context().length() > 2000
                || generated.expectedOutputFormat().length() > 64) {
            throw new InvalidAiOutputException("Generated challenge exceeds supported field limits");
        }
        validateList(generated.requirements(), "requirements");
        validateList(generated.constraints(), "constraints");
        validateList(generated.acceptanceCriteria(), "acceptanceCriteria");
    }

    private void validateList(List<String> values, String fieldName) {
        if (values == null || values.isEmpty() || values.size() > 30 || values.stream().anyMatch(v -> isBlank(v) || v.length() > 500)) {
            throw new InvalidAiOutputException("Challenge payload has invalid " + fieldName);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
