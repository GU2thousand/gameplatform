package com.gamingplatform.controller;

import com.gamingplatform.dto.CreateUserRequest;
import com.gamingplatform.dto.UserProgressResponse;
import com.gamingplatform.dto.UserResponse;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.service.ProgressService;
import com.gamingplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final ProgressService progressService;

    public UserController(UserService userService, ProgressService progressService) {
        this.userService = userService;
        this.progressService = progressService;
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        UserProfile user = userService.create(request.getUsername());
        return new UserResponse(user.getId(), user.getUsername(), user.getXp());
    }

    @GetMapping("/{id}/progress")
    public UserProgressResponse progress(@PathVariable("id") Long userId) {
        return progressService.getProgress(userId);
    }
}
