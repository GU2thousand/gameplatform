package com.gamingplatform.repository;

import com.gamingplatform.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    java.util.Optional<Submission> findByIdAndUser_IdAndChallenge_Id(Long id, Long userId, Long challengeId);

    @org.springframework.data.jpa.repository.Query("select count(distinct s.challenge.id) from Submission s where s.user.id = :userId")
    long countCompletedChallenges(@org.springframework.data.repository.query.Param("userId") Long userId);
    java.util.Optional<Submission> findFirstByUser_IdAndChallenge_IdAndAnswerHash(Long userId, Long challengeId, String hash);
    java.util.Optional<Submission> findFirstByUser_IdAndChallenge_IdOrderByIdDesc(Long userId, Long challengeId);
}
