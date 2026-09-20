package com.gamingplatform.service;

import com.gamingplatform.dto.ChallengeStateResponse;
import com.gamingplatform.dto.SubmissionResponse;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.exception.NotFoundException;
import com.gamingplatform.repository.ChallengeRepository;
import com.gamingplatform.repository.EvaluationRepository;
import com.gamingplatform.repository.SubmissionRepository;
import com.gamingplatform.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class WorkspaceService {
    private final ChallengeRepository challenges;
    private final SubmissionRepository submissions;
    private final EvaluationRepository evaluations;
    private final UserProfileRepository users;
    private final SubmissionService grading;
    public WorkspaceService(ChallengeRepository challenges, SubmissionRepository submissions, EvaluationRepository evaluations,
            UserProfileRepository users, SubmissionService grading) {
        this.challenges = challenges; this.submissions = submissions; this.evaluations = evaluations;
        this.users = users; this.grading = grading;
    }
    @Transactional(readOnly = true)
    public ChallengeStateResponse current(Long userId) {
        return challenges.findFirstByOwner_IdOrderByIdDesc(userId).map(this::state)
                .orElse(new ChallengeStateResponse(null, "", null));
    }
    @Transactional(readOnly = true)
    public ChallengeStateResponse get(Long id, Long userId) { return state(owned(id, userId)); }
    @Transactional(readOnly = true)
    public List<ChallengeStateResponse> history(Long userId) {
        return challenges.findTop20ByOwner_IdOrderByIdDesc(userId).stream().map(this::state).toList();
    }
    @Transactional
    public void saveDraft(Long id, Long userId, String answer) {
        users.lockById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        owned(id, userId).setDraftAnswer(answer);
    }
    private Challenge owned(Long id, Long userId) {
        return challenges.findByIdAndOwner_Id(id, userId).orElseThrow(() -> new NotFoundException("Challenge not found"));
    }
    private ChallengeStateResponse state(Challenge challenge) {
        var selected = challenge.getActiveSubmissionId() == null
                ? submissions.findFirstByUser_IdAndChallenge_IdOrderByIdDesc(challenge.getOwner().getId(), challenge.getId())
                : submissions.findByIdAndUser_IdAndChallenge_Id(challenge.getActiveSubmissionId(), challenge.getOwner().getId(), challenge.getId());
        SubmissionResponse result = selected.flatMap(s -> evaluations.findBySubmission_Id(s.getId())).map(grading::response).orElse(null);
        return new ChallengeStateResponse(ChallengeService.response(challenge), challenge.getDraftAnswer(), result);
    }
}
