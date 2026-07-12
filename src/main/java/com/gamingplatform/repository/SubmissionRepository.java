package com.gamingplatform.repository;

import com.gamingplatform.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamingplatform.entity.SubmissionStatus;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    long countByUser_Id(Long userId);

    long countByUser_IdAndStatus(Long userId, SubmissionStatus status);

    @Query("select count(distinct s.challenge.id) from Submission s where s.user.id = :userId " +
            "and s.status = com.gamingplatform.entity.SubmissionStatus.COMPLETED")
    long countDistinctCompletedChallenges(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"challenge"})
    Optional<Submission> findByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = {"challenge"})
    Optional<Submission> findByUser_IdAndIdempotencyKey(Long userId, String idempotencyKey);

    @EntityGraph(attributePaths = {"challenge"})
    List<Submission> findByUser_IdOrderBySubmittedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"challenge"})
    List<Submission> findByChallenge_IdAndUser_IdOrderBySubmittedAtDesc(Long challengeId, Long userId);

    List<Submission> findByUser_IdAndStatusOrderBySubmittedAtAsc(Long userId, SubmissionStatus status);

    List<Submission> findByStatusOrderBySubmittedAtAsc(SubmissionStatus status);

    Optional<Submission> findFirstByChallenge_IdAndUser_IdNotOrderByUser_IdAsc(Long challengeId, Long userId);

    boolean existsByChallenge_Id(Long challengeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Submission s set s.status = com.gamingplatform.entity.SubmissionStatus.PROCESSING, " +
            "s.processingStartedAt = :startedAt where s.id = :id " +
            "and s.status = com.gamingplatform.entity.SubmissionStatus.PENDING")
    int claimForProcessing(@Param("id") Long id, @Param("startedAt") java.time.Instant startedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Submission s set s.status = com.gamingplatform.entity.SubmissionStatus.PENDING, " +
            "s.processingStartedAt = null where s.status = com.gamingplatform.entity.SubmissionStatus.PROCESSING " +
            "and s.processingStartedAt < :cutoff")
    int recoverStaleProcessing(@Param("cutoff") java.time.Instant cutoff);

    @Modifying
    void deleteByUser_Id(Long userId);
}
