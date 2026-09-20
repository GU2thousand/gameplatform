package com.gamingplatform.controller;

import com.gamingplatform.dto.UserProgressResponse;
import com.gamingplatform.dto.ChallengeStateResponse;
import com.gamingplatform.exception.NotFoundException;
import com.gamingplatform.security.SessionService;
import com.gamingplatform.service.ProgressService;
import com.gamingplatform.service.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final ProgressService progress;
    private final WorkspaceService workspace;
    public UserController(ProgressService progress, WorkspaceService workspace) {
        this.progress = progress; this.workspace = workspace;
    }
    @GetMapping("/me/progress")
    public UserProgressResponse progress(HttpServletRequest request) {
        return progress.getProgress(SessionService.currentUser(request));
    }
    @GetMapping("/{id}/progress")
    public UserProgressResponse legacyProgress(@PathVariable Long id, HttpServletRequest request) {
        if (!id.equals(SessionService.currentUser(request))) throw new NotFoundException("User not found");
        return progress.getProgress(id);
    }
    @GetMapping("/me/history")
    public List<ChallengeStateResponse> history(HttpServletRequest request) {
        return workspace.history(SessionService.currentUser(request));
    }
}
