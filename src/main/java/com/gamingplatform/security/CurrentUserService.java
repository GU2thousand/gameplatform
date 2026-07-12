package com.gamingplatform.security;

import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {
    private final UserService userService;

    public CurrentUserService(UserService userService) {
        this.userService = userService;
    }

    public UserProfile requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                    "Authentication required");
        }
        return userService.getByUsername(authentication.getName());
    }

    public Long requireUserId() {
        return requireUser().getId();
    }
}
