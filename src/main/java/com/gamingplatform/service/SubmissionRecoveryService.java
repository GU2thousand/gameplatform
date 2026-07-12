package com.gamingplatform.service;

import com.gamingplatform.entity.SubmissionStatus;
import com.gamingplatform.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;

@Component
public class SubmissionRecoveryService {
    private final SubmissionRepository submissions;
    private final SubmissionProcessor processor;
    private final Executor executor;
    private final TransactionTemplate transactions;

    public SubmissionRecoveryService(SubmissionRepository submissions, SubmissionProcessor processor,
                                     @Qualifier("submissionExecutor") Executor executor,
                                     PlatformTransactionManager transactionManager) {
        this.submissions = submissions;
        this.processor = processor;
        this.executor = executor;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() { recover(); }

    @Scheduled(fixedDelayString = "${app.submissions.recovery-interval-ms:300000}")
    public void recover() {
        List<Long> pending = transactions.execute(status -> {
            submissions.recoverStaleProcessing(Instant.now().minus(Duration.ofMinutes(10)));
            return submissions.findByStatusOrderBySubmittedAtAsc(SubmissionStatus.PENDING).stream()
                    .map(com.gamingplatform.entity.Submission::getId).toList();
        });
        if (pending != null) pending.forEach(id -> executor.execute(() -> processor.process(id)));
    }
}
