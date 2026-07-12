package com.gamingplatform.repository;

import com.gamingplatform.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    @Query("select avg(e.finalScore) from Evaluation e where e.submission.user.id = :userId")
    Double findAverageFinalScoreByUserId(@Param("userId") Long userId);

    Optional<Evaluation> findBySubmission_Id(Long submissionId);

    List<Evaluation> findBySubmission_User_Id(Long userId);

    @Query("select max(e.finalScore) from Evaluation e where e.submission.user.id = :userId and e.submission.challenge.id = :challengeId")
    Double findBestScoreForChallenge(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

    void deleteBySubmission_User_Id(Long userId);

    @Query("""
            select avg(e.finalScore) as averageFinalScore,
                   avg(e.requirementUnderstanding) as requirementUnderstanding,
                   avg(e.logicalClarity) as logicalClarity,
                   avg(e.technicalFeasibility) as technicalFeasibility,
                   avg(e.edgeCaseCoverage) as edgeCaseCoverage,
                   avg(e.communicationStructure) as communicationStructure
            from Evaluation e
            where e.submission.user.id = :userId
            """)
    EvaluationAverages findAveragesByUserId(@Param("userId") Long userId);

    interface EvaluationAverages {

        Double getAverageFinalScore();

        Double getRequirementUnderstanding();

        Double getLogicalClarity();

        Double getTechnicalFeasibility();

        Double getEdgeCaseCoverage();

        Double getCommunicationStructure();
    }
}
