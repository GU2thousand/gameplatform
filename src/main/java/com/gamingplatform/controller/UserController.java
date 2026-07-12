package com.gamingplatform.controller;

import com.gamingplatform.dto.UserProgressResponse;
import com.gamingplatform.security.CurrentUserService;
import com.gamingplatform.service.ProgressService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    private final ProgressService progressService;
    private final CurrentUserService currentUser;

    public UserController(ProgressService progressService, CurrentUserService currentUser) {
        this.progressService = progressService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me/progress")
    public UserProgressResponse myProgress() {
        return progressService.getProgress(currentUser.requireUserId());
    }

    @GetMapping("/{id}/progress")
    public UserProgressResponse progress(@PathVariable("id") @Positive Long userId) {
        Long authenticated = currentUser.requireUserId();
        if (!authenticated.equals(userId)) {
            throw new com.gamingplatform.exception.NotFoundException("User not found: " + userId);
        }
        return progressService.getProgress(authenticated);
    }
}
