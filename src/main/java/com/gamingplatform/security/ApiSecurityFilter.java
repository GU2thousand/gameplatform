package com.gamingplatform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApiSecurityFilter extends OncePerRequestFilter {
    private final SessionService sessions;
    private final ObjectMapper mapper;
    private final int expensiveLimit;
    private final Map<String, Window> windows = new HashMap<>();
    private record Window(long expiresAt, int count) {}

    public ApiSecurityFilter(SessionService sessions, ObjectMapper mapper,
            @Value("${app.rate-limit.expensive-per-minute:12}") int expensiveLimit) {
        this.sessions = sessions;
        this.mapper = mapper;
        this.expensiveLimit = expensiveLimit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'");
        String path = request.getRequestURI();
        // Reject ambiguous servlet paths before MVC can normalize matrix parameters or encoded separators.
        if (path.contains(";") || path.contains("%") || path.contains("\\") || path.contains("//")
                || path.contains("/./") || path.contains("/../")) {
            reject(response, 400, "Non-canonical request path", path); return;
        }
        if (!path.startsWith("/api/")) { chain.doFilter(request, response); return; }
        response.setHeader("Cache-Control", "no-store");
        boolean mutation = !request.getMethod().equals("GET") && !request.getMethod().equals("HEAD");
        if (mutation) {
            if (!"career-platform".equals(request.getHeader("X-Requested-With")) || !sameOrigin(request)) {
                reject(response, 403, "Same-origin API request required", path); return;
            }
            if (request.getContentType() == null || !request.getContentType().toLowerCase().startsWith("application/json")) {
                reject(response, 415, "Content-Type must be application/json", path); return;
            }
            byte[] body = request.getInputStream().readNBytes(32769);
            if (body.length > 32768) { reject(response, 413, "Request body exceeds 32 KiB", path); return; }
            request = buffered(request, body);
        }
        boolean sessionStart = path.equals("/api/session") && request.getMethod().equals("POST");
        boolean publicMode = path.equals("/api/debug/ai-mode") && request.getMethod().equals("GET");
        var user = sessions.resolve(request);
        if (!sessionStart && !publicMode && user.isEmpty()) {
            reject(response, 401, "Start a browser session first", path); return;
        }
        if (user.isPresent()) request.setAttribute(SessionService.USER_ATTRIBUTE, user.get().getId());
        if (mutation) {
            String identity = user.map(u -> "user:" + u.getId()).orElse("ip:" + request.getRemoteAddr());
            boolean expensive = path.equals("/api/submission") || path.equals("/api/challenge/generate");
            int limit = expensive ? expensiveLimit : (sessionStart && user.isEmpty() ? 20 : 120);
            long duration = sessionStart && user.isEmpty() ? 3_600_000L : 60_000L;
            if (!allow(identity + (expensive ? ":expensive" : ":write"), limit, duration)) {
                response.setHeader("Retry-After", Long.toString(duration / 1000));
                reject(response, 429, "Too many requests; please wait and retry", path); return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean sameOrigin(HttpServletRequest request) {
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if (fetchSite != null && !fetchSite.equals("same-origin") && !fetchSite.equals("none")) return false;
        String origin = request.getHeader("Origin");
        if (origin == null) return true; // Non-browser clients still need the custom header.
        try {
            URI uri = URI.create(origin);
            int port = uri.getPort() == -1 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
            return request.getScheme().equals(uri.getScheme()) && uri.getHost() != null
                    && uri.getHost().equalsIgnoreCase(request.getServerName()) && port == request.getServerPort()
                    && (uri.getPath() == null || uri.getPath().isEmpty()) && uri.getUserInfo() == null;
        } catch (IllegalArgumentException ex) { return false; }
    }

    private synchronized boolean allow(String key, int limit, long duration) {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        Window current = windows.get(key);
        if (current == null) {
            if (windows.size() >= 10000) return false;
            windows.put(key, new Window(now + duration, 1));
            return true;
        }
        if (current.count() >= limit) return false;
        windows.put(key, new Window(current.expiresAt(), current.count() + 1));
        return true;
    }

    private HttpServletRequest buffered(HttpServletRequest request, byte[] bytes) {
        return new HttpServletRequestWrapper(request) {
            @Override public ServletInputStream getInputStream() {
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                return new ServletInputStream() {
                    public int read() { return stream.read(); }
                    public boolean isFinished() { return stream.available() == 0; }
                    public boolean isReady() { return true; }
                    public void setReadListener(ReadListener listener) { throw new UnsupportedOperationException(); }
                };
            }
        };
    }

    private void reject(HttpServletResponse response, int code, String message, String path) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), code,
                HttpStatus.valueOf(code).getReasonPhrase(), message, path));
    }
}
