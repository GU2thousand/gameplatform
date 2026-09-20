package com.gamingplatform.repository;

import com.gamingplatform.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    java.util.Optional<Challenge> findByIdAndOwner_Id(Long id, Long ownerId);
    java.util.Optional<Challenge> findFirstByOwner_IdOrderByIdDesc(Long ownerId);
    java.util.List<Challenge> findTop20ByOwner_IdOrderByIdDesc(Long ownerId);
}
