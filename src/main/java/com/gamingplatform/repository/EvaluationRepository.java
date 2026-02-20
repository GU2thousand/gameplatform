package com.gamingplatform.repository;

import com.gamingplatform.entity.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    @Query("select avg(e.finalScore) from Evaluation e where e.submission.user.id = :userId")
    Double findAverageFinalScoreByUserId(@Param("userId") Long userId);

    @Query("select e from Evaluation e where e.submission.user.id = :userId order by e.createdAt desc")
    Page<Evaluation> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select e from Evaluation e where e.submission.user.id = :userId")
    List<Evaluation> findAllByUserId(@Param("userId") Long userId);
}
