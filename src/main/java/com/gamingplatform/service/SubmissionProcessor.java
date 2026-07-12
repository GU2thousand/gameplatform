package com.gamingplatform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.entity.Evaluation;
import com.gamingplatform.entity.RubricDimension;
import com.gamingplatform.entity.Submission;
import com.gamingplatform.entity.SubmissionStatus;
import com.gamingplatform.repository.EvaluationRepository;
import com.gamingplatform.repository.SubmissionRepository;
import com.gamingplatform.repository.UserProfileRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Map;

@Component
public class SubmissionProcessor {
    private static final Logger log = LoggerFactory.getLogger(SubmissionProcessor.class);
    private final SubmissionRepository submissions;
    private final EvaluationRepository evaluations;
    private final UserProfileRepository users;
    private final EvaluationEngine engine;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meters;
    private final TransactionTemplate transactions;

    public SubmissionProcessor(SubmissionRepository submissions, EvaluationRepository evaluations,
                               UserProfileRepository users, EvaluationEngine engine, ObjectMapper objectMapper,
                               MeterRegistry meters, PlatformTransactionManager transactionManager) {
        this.submissions = submissions;
        this.evaluations = evaluations;
        this.users = users;
        this.engine = engine;
        this.objectMapper = objectMapper;
        this.meters = meters;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Async("submissionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQueued(SubmissionQueuedEvent event) {
        process(event.submissionId());
    }

    public void process(Long submissionId) {
        try {
            Boolean claimed = transactions.execute(status ->
                    submissions.claimForProcessing(submissionId, Instant.now()) == 1);
            if (!Boolean.TRUE.equals(claimed)) return;

            Work work = transactions.execute(status -> loadWork(submissionId));
            EvaluationEngine.EvaluatedAnswer result = engine.evaluate(work.challenge(), work.answer());
            transactions.executeWithoutResult(status -> complete(work, result));
            meters.counter("gamingplatform.submissions", "status", "completed").increment();
        } catch (RuntimeException failure) {
            log.error("Async evaluation failed for submission {}", submissionId, failure);
            transactions.executeWithoutResult(status -> markFailed(submissionId, failure));
            meters.counter("gamingplatform.submissions", "status", "failed").increment();
        }
    }

    private Work loadWork(Long id) {
        Submission submission = submissions.findById(id)
                .orElseThrow(() -> new IllegalStateException("Submission disappeared: " + id));
        Challenge challenge = submission.getChallenge();
        challenge.getRequirements().size();
        challenge.getConstraints().size();
        challenge.getAcceptanceCriteria().size();
        return new Work(submission.getId(), submission.getUser().getId(), challenge, submission.getAnswer());
    }

    private void complete(Work work, EvaluationEngine.EvaluatedAnswer result) {
        Submission submission = submissions.findById(work.submissionId())
                .orElseThrow(() -> new IllegalStateException("Submission disappeared: " + work.submissionId()));
        if (submission.getStatus() != SubmissionStatus.PROCESSING) return;

        Double previousBest = evaluations.findBestScoreForChallenge(work.userId(), work.challenge().getId());
        Evaluation evaluation = new Evaluation();
        evaluation.setSubmission(submission);
        evaluation.setRequirementUnderstanding(result.scores().get(RubricDimension.REQUIREMENT_UNDERSTANDING));
        evaluation.setLogicalClarity(result.scores().get(RubricDimension.LOGICAL_CLARITY));
        evaluation.setTechnicalFeasibility(result.scores().get(RubricDimension.TECHNICAL_FEASIBILITY));
        evaluation.setEdgeCaseCoverage(result.scores().get(RubricDimension.EDGE_CASE_COVERAGE));
        evaluation.setCommunicationStructure(result.scores().get(RubricDimension.COMMUNICATION_STRUCTURE));
        evaluation.setFinalScore(result.finalScore());
        evaluation.setFeedback(result.feedback());
        evaluation.setProvider(result.provider());
        evaluation.setStrengthsJson(json(result.strengths()));
        evaluation.setImprovementsJson(json(result.improvements()));
        evaluation.setExampleOutline(result.exampleOutline());
        evaluation.setRubricWeightsJson(json(result.weights()));
        evaluations.save(evaluation);

        int xp = previousBest == null
                ? (int) Math.round(result.finalScore())
                : Math.max(0, (int) Math.round(result.finalScore() - previousBest));
        if (xp > 0 && users.incrementXp(work.userId(), xp) != 1) {
            throw new IllegalStateException("Unable to award XP");
        }
        submission.setStatus(SubmissionStatus.COMPLETED);
        submission.setCompletedAt(Instant.now());
        submission.setErrorMessage(null);
        submissions.save(submission);
    }

    private void markFailed(Long id, RuntimeException failure) {
        submissions.findById(id).ifPresent(submission -> {
            if (submission.getStatus() == SubmissionStatus.COMPLETED) return;
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setCompletedAt(Instant.now());
            String message = failure.getMessage() == null ? "Evaluation failed" : failure.getMessage();
            submission.setErrorMessage(message.substring(0, Math.min(message.length(), 1000)));
            submissions.save(submission);
        });
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize evaluation", e);
        }
    }

    private record Work(Long submissionId, Long userId, Challenge challenge, String answer) {}
}
