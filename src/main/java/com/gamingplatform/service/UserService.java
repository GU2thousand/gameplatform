package com.gamingplatform.service;

import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.exception.ConflictException;
import com.gamingplatform.exception.NotFoundException;
import com.gamingplatform.repository.UserProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder) {
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfile getById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserProfile create(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        String normalizedUsername = username.trim();
        userProfileRepository.findByUsername(normalizedUsername).ifPresent(existing -> {
            throw new ConflictException("Username already exists: " + normalizedUsername);
        });

        UserProfile user = new UserProfile();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        try {
            return userProfileRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Username already exists: " + normalizedUsername, ex);
        }
    }

    @Transactional(readOnly = true)
    public UserProfile getByUsername(String username) {
        return userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public UserProfile claimLegacyAccount(String username, String newPassword) {
        UserProfile user = userProfileRepository.findByUsername(username.trim())
                .orElseThrow(() -> new NotFoundException("Legacy account not found"));
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            throw new ConflictException("Account has already been secured");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userProfileRepository.saveAndFlush(user);
    }
}
