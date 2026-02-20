package com.gamingplatform.service;

import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.exception.NotFoundException;
import com.gamingplatform.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserProfile getById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserProfile create(String username) {
        userProfileRepository.findByUsername(username).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists: " + username);
        });

        UserProfile user = new UserProfile();
        user.setUsername(username);
        return userProfileRepository.save(user);
    }
}
