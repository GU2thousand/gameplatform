package com.gamingplatform.service;

import com.gamingplatform.ai.ChallengeAiClient;
import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.ai.GeneratedChallenge;
import com.gamingplatform.ai.impl.TemplateChallengeAiClient;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.exception.AiServiceException;
import com.gamingplatform.exception.InvalidAiOutputException;
import com.gamingplatform.exception.NotFoundException;
import com.gamingplatform.repository.ChallengeRepository;
import com.gamingplatform.entity.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_CONTEXT_LENGTH = 2000;
    private static final int MAX_LIST_ITEMS = 20;
    private static final int MAX_LIST_ITEM_LENGTH = 500;
    private static final int MAX_OUTPUT_FORMAT_LENGTH = 64;

    private final ChallengeAiClient challengeAiClient;
    private final TemplateChallengeAiClient fallbackChallengeAiClient;
    private final ChallengeRepository challengeRepository;
    private final AiQuotaService aiQuotaService;

    public ChallengeService(
            ChallengeAiClient challengeAiClient,
            TemplateChallengeAiClient fallbackChallengeAiClient,
            ChallengeRepository challengeRepository,
            AiQuotaService aiQuotaService
    ) {
        this.challengeAiClient = challengeAiClient;
        this.fallbackChallengeAiClient = fallbackChallengeAiClient;
        this.challengeRepository = challengeRepository;
        this.aiQuotaService = aiQuotaService;
    }

    public Challenge generate(ChallengeGenerationInput input) {
        return generate(input, null);
    }

    public Challenge generate(ChallengeGenerationInput input, UserProfile owner) {
        if (owner != null) aiQuotaService.consume(owner.getId(), "challenge", java.util.UUID.randomUUID().toString());
        GeneratedWithProvider generatedWithProvider = generateWithFallback(input);
        GeneratedChallenge generated = generatedWithProvider.challenge();

        Challenge challenge = new Challenge();
        challenge.setCreatedBy(owner);
        challenge.setTitle(generated.title().trim());
        challenge.setDifficulty(generated.difficulty());
        challenge.setContext(generated.context().trim());
        challenge.setRequirements(trimmedCopy(generated.requirements()));
        challenge.setConstraints(trimmedCopy(generated.constraints()));
        challenge.setAcceptanceCriteria(trimmedCopy(generated.acceptanceCriteria()));
        challenge.setExpectedOutputFormat(generated.expectedOutputFormat().trim());
        challenge.setRoleTrack(trimToNull(input == null ? null : input.roleTrack()));
        challenge.setChallengeType(trimToNull(input == null ? null : input.challengeType()));
        challenge.setFocusGoal(trimToNull(input == null ? null : input.focusGoal()));
        challenge.setGenerationProvider(generatedWithProvider.provider());

        return challengeRepository.save(challenge);
    }

    @Transactional(readOnly = true)
    public Challenge getOwnedById(Long challengeId, Long userId) {
        Challenge challenge = challengeRepository.findAccessibleById(challengeId, userId)
                .orElseThrow(() -> new NotFoundException("Challenge not found: " + challengeId));
        initializeDetails(challenge);
        return challenge;
    }

    @Transactional(readOnly = true)
    public List<Challenge> history(Long userId) {
        List<Challenge> challenges = challengeRepository.findAccessibleByUser(userId);
        challenges.forEach(this::initializeDetails);
        return challenges;
    }

    @Transactional(readOnly = true)
    public Challenge getById(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found: " + challengeId));
        initializeDetails(challenge);
        return challenge;
    }

    private void initializeDetails(Challenge challenge) {
        challenge.getRequirements().size();
        challenge.getConstraints().size();
        challenge.getAcceptanceCriteria().size();
    }

    private GeneratedWithProvider generateWithFallback(ChallengeGenerationInput input) {
        try {
            GeneratedChallenge generated = challengeAiClient.generate(input);
            validateGeneratedChallenge(generated);
            return new GeneratedWithProvider(generated, challengeAiClient == fallbackChallengeAiClient
                    ? "local" : "langchain4j");
        } catch (RuntimeException primaryFailure) {
            if (challengeAiClient == fallbackChallengeAiClient) {
                throw new AiServiceException("Challenge generation failed", primaryFailure);
            }

            log.warn(
                    "Primary challenge AI client {} failed; using local fallback: {}",
                    challengeAiClient.getClass().getSimpleName(),
                    primaryFailure.getClass().getSimpleName()
            );
            try {
                GeneratedChallenge fallback = fallbackChallengeAiClient.generate(input);
                validateGeneratedChallenge(fallback);
                return new GeneratedWithProvider(fallback, "fallback");
            } catch (RuntimeException fallbackFailure) {
                primaryFailure.addSuppressed(fallbackFailure);
                throw new AiServiceException("Challenge generation failed", primaryFailure);
            }
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private record GeneratedWithProvider(GeneratedChallenge challenge, String provider) {}

    private void validateGeneratedChallenge(GeneratedChallenge generated) {
        if (generated == null) {
            throw new InvalidAiOutputException("Challenge generation returned null payload");
        }
        validateText(generated.title(), "title", MAX_TITLE_LENGTH);
        if (generated.difficulty() == null) {
            throw new InvalidAiOutputException("Challenge payload missing difficulty");
        }
        validateText(generated.context(), "context", MAX_CONTEXT_LENGTH);
        validateList(generated.requirements(), "requirements");
        validateList(generated.constraints(), "constraints");
        validateList(generated.acceptanceCriteria(), "acceptanceCriteria");
        validateText(generated.expectedOutputFormat(), "expectedOutputFormat", MAX_OUTPUT_FORMAT_LENGTH);
    }

    private void validateList(List<String> values, String fieldName) {
        if (values == null || values.isEmpty() || values.size() > MAX_LIST_ITEMS) {
            throw new InvalidAiOutputException("Challenge payload has invalid " + fieldName);
        }
        for (String value : values) {
            validateText(value, fieldName + " item", MAX_LIST_ITEM_LENGTH);
        }
    }

    private void validateText(String value, String fieldName, int maxLength) {
        if (isBlank(value) || value.trim().length() > maxLength) {
            throw new InvalidAiOutputException("Challenge payload has invalid " + fieldName);
        }
    }

    private List<String> trimmedCopy(List<String> values) {
        List<String> result = new ArrayList<>(values.size());
        for (String value : values) {
            result.add(value.trim());
        }
        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
