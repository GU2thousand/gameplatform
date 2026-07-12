package com.gamingplatform.repository;

import com.gamingplatform.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUsername(String username);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update UserProfile u set u.xp = u.xp + :delta where u.id = :userId")
    int incrementXp(@Param("userId") Long userId, @Param("delta") int delta);
}
