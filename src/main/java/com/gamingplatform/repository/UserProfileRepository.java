package com.gamingplatform.repository;

import com.gamingplatform.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUsername(String username);
    Optional<UserProfile> findBySessionTokenHash(String hash);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UserProfile u where u.id = :id")
    Optional<UserProfile> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
