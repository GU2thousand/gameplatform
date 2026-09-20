package com.gamingplatform.controller;
import com.gamingplatform.dto.UserResponse;
import com.gamingplatform.security.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/session")
public class SessionController {
    private final SessionService sessions;
    public SessionController(SessionService sessions) { this.sessions = sessions; }
    @PostMapping public UserResponse start(HttpServletRequest request, HttpServletResponse response) {
        return sessions.start(request, response);
    }
}
