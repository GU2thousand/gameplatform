package com.gamingplatform.config;

import com.gamingplatform.exception.RateLimitException;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final UsageProperties properties;
    private final MeterRegistry meters;
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public RateLimitInterceptor(UsageProperties properties, MeterRegistry meters) {
        this.properties = properties;
        this.meters = meters;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!request.getRequestURI().startsWith("/api/") || request.getRequestURI().equals("/api/auth/csrf")) {
            return true;
        }
        long minute = Instant.now().getEpochSecond() / 60;
        String principal = request.getUserPrincipal() == null
                ? "ip:" + request.getRemoteAddr() : "user:" + request.getUserPrincipal().getName();
        String endpoint = request.getMethod() + ":" + request.getRequestURI();
        String key = minute + ":" + principal + ":" + endpoint;
        int count = counters.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
        if (count > properties.getRequestsPerMinute()) {
            meters.counter("gamingplatform.rate_limit", "scope", "endpoint").increment();
            throw new RateLimitException("Too many requests; try again shortly");
        }
        if (counters.size() > 10_000) counters.keySet().removeIf(existing -> !existing.startsWith(minute + ":"));
        return true;
    }
}
