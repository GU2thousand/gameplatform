package com.gamingplatform.controller;

import com.gamingplatform.ai.ChallengeGenerationInput;
import com.gamingplatform.dto.*;
import com.gamingplatform.security.SessionService;
import com.gamingplatform.service.ChallengeService;
import com.gamingplatform.service.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/challenge")
public class ChallengeController {
    private final ChallengeService challenges;
    private final WorkspaceService workspace;
    private final com.gamingplatform.service.AiWorkLimiter limiter;
    public ChallengeController(ChallengeService challenges, WorkspaceService workspace, com.gamingplatform.service.AiWorkLimiter limiter) {
        this.challenges = challenges; this.workspace = workspace; this.limiter = limiter;
    }
    @PostMapping("/generate")
    public ChallengeResponse generate(@Valid @RequestBody(required = false) ChallengeGenerateRequest request, HttpServletRequest http) {
        if (request == null) request = new ChallengeGenerateRequest();
        var input = new ChallengeGenerationInput(request.getDifficulty(), request.getRoleTrack(), request.getChallengeType(),
                request.getFocusGoal(), request.getBusinessContext(), request.getCustomRequirements(),
                request.getCustomConstraints(), request.getCustomAcceptanceCriteria());
        return limiter.execute(() -> challenges.generate(input, SessionService.currentUser(http)));
    }
    @GetMapping("/current")
    public ChallengeStateResponse current(HttpServletRequest request) {
        return workspace.current(SessionService.currentUser(request));
    }
    @GetMapping("/{id}")
    public ChallengeStateResponse get(@PathVariable Long id, HttpServletRequest request) {
        return workspace.get(id, SessionService.currentUser(request));
    }
    @PutMapping("/{id}/draft")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveDraft(@PathVariable Long id, @Valid @RequestBody DraftRequest draft, HttpServletRequest request) {
        limiter.execute(() -> { workspace.saveDraft(id, SessionService.currentUser(request), draft.answer()); return null; });
    }
}
