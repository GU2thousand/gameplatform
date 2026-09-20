package com.gamingplatform.security;

import com.gamingplatform.dto.UserResponse;
import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.repository.UserProfileRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class SessionService {
    public static final String COOKIE = "gp_session";
    public static final String USER_ATTRIBUTE = SessionService.class.getName() + ".userId";
    private final UserProfileRepository users;
    private final boolean forceSecure;
    private final SecureRandom random = new SecureRandom();

    public SessionService(UserProfileRepository users, @Value("${app.session.cookie-secure:false}") boolean forceSecure) {
        this.users = users;
        this.forceSecure = forceSecure;
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        for (Cookie cookie : cookies) {
            if (COOKIE.equals(cookie.getName()) && cookie.getValue().matches("[A-Za-z0-9_-]{43}")) {
                return users.findBySessionTokenHash(hash(cookie.getValue()))
                        .filter(u -> u.getSessionExpiresAt() != null && u.getSessionExpiresAt().isAfter(Instant.now()));
            }
        }
        return Optional.empty();
    }

    @Transactional
    public UserResponse start(HttpServletRequest request, HttpServletResponse response) {
        UserProfile user = resolve(request).orElseGet(() -> {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            UserProfile created = new UserProfile();
            created.setUsername("Learner-" + java.util.UUID.randomUUID());
            created.setSessionTokenHash(hash(token));
            created.setSessionExpiresAt(Instant.now().plus(Duration.ofDays(365)));
            users.save(created);
            response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(COOKIE, token)
                    .httpOnly(true).secure(forceSecure || request.isSecure()).sameSite("Strict")
                    .path("/").maxAge(Duration.ofDays(365)).build().toString());
            return created;
        });
        return new UserResponse(user.getId(), user.getUsername(), user.getXp());
    }

    public static Long currentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(USER_ATTRIBUTE);
        if (userId == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Start a browser session first");
        return userId;
    }

    public static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
