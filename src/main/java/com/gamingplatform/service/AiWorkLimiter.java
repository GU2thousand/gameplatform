package com.gamingplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

@Component
public class AiWorkLimiter {
    private final Semaphore slots;
    public AiWorkLimiter(@Value("${app.ai.max-concurrent-requests:4}") int maximum) {
        if (maximum < 1) throw new IllegalArgumentException("AI concurrency limit must be positive");
        slots = new Semaphore(maximum);
    }
    public <T> T execute(Supplier<T> operation) {
        if (!slots.tryAcquire()) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Training service is busy; please retry shortly");
        try { return operation.get(); } finally { slots.release(); }
    }
}
