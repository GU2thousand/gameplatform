package com.gamingplatform.repository;

import com.gamingplatform.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);
    Optional<Challenge> findByIdAndCreatedBy_Id(Long id, Long userId);
    void deleteByCreatedBy_Id(Long userId);

    @Query("select distinct c from Challenge c where c.createdBy.id = :userId or exists " +
            "(select s.id from Submission s where s.challenge = c and s.user.id = :userId) order by c.createdAt desc")
    List<Challenge> findAccessibleByUser(@Param("userId") Long userId);

    @Query("select c from Challenge c where c.id = :id and (c.createdBy.id = :userId or exists " +
            "(select s.id from Submission s where s.challenge = c and s.user.id = :userId))")
    Optional<Challenge> findAccessibleById(@Param("id") Long id, @Param("userId") Long userId);
}
