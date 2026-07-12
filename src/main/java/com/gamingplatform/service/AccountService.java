package com.gamingplatform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.Evaluation;
import com.gamingplatform.entity.Submission;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.repository.ChallengeRepository;
import com.gamingplatform.repository.EvaluationRepository;
import com.gamingplatform.repository.SubmissionRepository;
import com.gamingplatform.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

@Service
public class AccountService {
    private final UserService users;
    private final ChallengeRepository challenges;
    private final SubmissionRepository submissions;
    private final EvaluationRepository evaluations;
    private final UserProfileRepository userRepository;
    private final ObjectMapper objectMapper;

    public AccountService(UserService users, ChallengeRepository challenges, SubmissionRepository submissions,
                          EvaluationRepository evaluations, UserProfileRepository userRepository,
                          ObjectMapper objectMapper) {
        this.users = users;
        this.challenges = challenges;
        this.submissions = submissions;
        this.evaluations = evaluations;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> export(Long userId) {
        UserProfile user = users.getById(userId);
        Map<Long, Evaluation> bySubmission = new LinkedHashMap<>();
        evaluations.findBySubmission_User_Id(userId)
                .forEach(evaluation -> bySubmission.put(evaluation.getSubmission().getId(), evaluation));
        List<Map<String, Object>> challengeRows = challenges.findAccessibleByUser(userId).stream()
                .map(this::challengeExport).toList();
        List<Map<String, Object>> submissionRows = submissions.findByUser_IdOrderBySubmittedAtDesc(userId).stream()
                .map(submission -> submissionExport(submission, bySubmission.get(submission.getId()))).toList();
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("xp", user.getXp());
        profile.put("createdAt", user.getCreatedAt());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schemaVersion", 2);
        result.put("exportedAt", Instant.now());
        result.put("profile", profile);
        result.put("challenges", challengeRows);
        result.put("submissions", submissionRows);
        return result;
    }

    @Transactional
    public void delete(Long userId) {
        users.getById(userId);
        List<Challenge> accessibleBeforeDelete = challenges.findAccessibleByUser(userId);
        challenges.findByCreatedBy_IdOrderByCreatedAtDesc(userId).forEach(challenge ->
                submissions.findFirstByChallenge_IdAndUser_IdNotOrderByUser_IdAsc(challenge.getId(), userId)
                        .ifPresent(otherSubmission -> {
                            challenge.setCreatedBy(otherSubmission.getUser());
                            challenges.save(challenge);
                        }));
        evaluations.deleteBySubmission_User_Id(userId);
        submissions.deleteByUser_Id(userId);
        accessibleBeforeDelete.stream().filter(challenge -> challenge.getCreatedBy() == null)
                .filter(challenge -> !submissions.existsByChallenge_Id(challenge.getId()))
                .forEach(challenges::delete);
        challenges.deleteByCreatedBy_Id(userId);
        userRepository.deleteById(userId);
    }

    private Map<String, Object> challengeExport(Challenge challenge) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", challenge.getId());
        map.put("title", challenge.getTitle());
        map.put("difficulty", challenge.getDifficulty());
        map.put("context", challenge.getContext());
        map.put("requirements", List.copyOf(challenge.getRequirements()));
        map.put("constraints", List.copyOf(challenge.getConstraints()));
        map.put("acceptanceCriteria", List.copyOf(challenge.getAcceptanceCriteria()));
        map.put("roleTrack", challenge.getRoleTrack());
        map.put("challengeType", challenge.getChallengeType());
        map.put("focusGoal", challenge.getFocusGoal());
        map.put("generationProvider", challenge.getGenerationProvider());
        map.put("createdAt", challenge.getCreatedAt());
        return map;
    }

    private Map<String, Object> submissionExport(Submission submission, Evaluation evaluation) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", submission.getId());
        map.put("challengeId", submission.getChallenge().getId());
        map.put("answer", submission.getAnswer());
        map.put("status", submission.getStatus());
        map.put("submittedAt", submission.getSubmittedAt());
        map.put("completedAt", submission.getCompletedAt());
        if (evaluation != null) {
            Map<String, Object> evaluationMap = new LinkedHashMap<>();
            evaluationMap.put("id", evaluation.getId());
            evaluationMap.put("finalScore", evaluation.getFinalScore());
            evaluationMap.put("feedback", evaluation.getFeedback());
            evaluationMap.put("provider", evaluation.getProvider());
            evaluationMap.put("strengths", readList(evaluation.getStrengthsJson()));
            evaluationMap.put("improvements", readList(evaluation.getImprovementsJson()));
            evaluationMap.put("exampleOutline", evaluation.getExampleOutline());
            map.put("evaluation", evaluationMap);
        }
        return map;
    }

    private List<String> readList(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception ignored) { return List.of(); }
    }
}
