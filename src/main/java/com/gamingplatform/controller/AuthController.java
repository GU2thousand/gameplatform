package com.gamingplatform.controller;

import com.gamingplatform.dto.AuthRequest;
import com.gamingplatform.dto.CsrfResponse;
import com.gamingplatform.dto.UserResponse;
import com.gamingplatform.dto.LegacyClaimRequest;
import com.gamingplatform.config.AppSecurityProperties;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.security.CurrentUserService;
import com.gamingplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.BadCredentialsException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final AppSecurityProperties securityProperties;
    private final HttpSessionSecurityContextRepository contextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService, CurrentUserService currentUserService,
                          AuthenticationManager authenticationManager,
                          SessionAuthenticationStrategy sessionAuthenticationStrategy,
                          AppSecurityProperties securityProperties) {
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityProperties = securityProperties;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getToken(), csrfToken.getHeaderName(), csrfToken.getParameterName());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody AuthRequest request,
                                 HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        UserProfile user = userService.create(request.getUsername(), request.getPassword());
        authenticate(request, servletRequest, servletResponse);
        return toResponse(user);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody AuthRequest request,
                              HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        authenticate(request, servletRequest, servletResponse);
        return toResponse(userService.getByUsername(request.getUsername()));
    }

    @PostMapping("/claim-legacy")
    public UserResponse claimLegacy(@Valid @RequestBody LegacyClaimRequest request,
                                    HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String configured = securityProperties.getLegacyClaimSecret();
        if (configured.isBlank() || !MessageDigest.isEqual(configured.getBytes(StandardCharsets.UTF_8),
                request.getClaimSecret().getBytes(StandardCharsets.UTF_8))) {
            throw new BadCredentialsException("Invalid legacy claim credentials");
        }
        UserProfile user = userService.claimLegacyAccount(request.getUsername(), request.getNewPassword());
        AuthRequest auth = new AuthRequest();
        auth.setUsername(request.getUsername());
        auth.setPassword(request.getNewPassword());
        authenticate(auth, servletRequest, servletResponse);
        return toResponse(user);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return toResponse(currentUserService.requireUser());
    }

    private void authenticate(AuthRequest request, HttpServletRequest servletRequest,
                              HttpServletResponse servletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword()));
        sessionAuthenticationStrategy.onAuthentication(authentication, servletRequest, servletResponse);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, servletRequest, servletResponse);
    }

    private UserResponse toResponse(UserProfile user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getXp());
    }
}
