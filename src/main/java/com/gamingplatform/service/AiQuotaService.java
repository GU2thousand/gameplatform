package com.gamingplatform.service;

import com.gamingplatform.config.UsageProperties;
import com.gamingplatform.exception.RateLimitException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiQuotaService {
    private final UsageProperties properties;
    private final MeterRegistry meters;
    private final ConcurrentHashMap<String, AtomicInteger> usage = new ConcurrentHashMap<>();
    private final Set<String> deduplicatedOperations = ConcurrentHashMap.newKeySet();

    public AiQuotaService(UsageProperties properties, MeterRegistry meters) {
        this.properties = properties;
        this.meters = meters;
    }

    public void consume(Long userId, String operation, String operationKey) {
        LocalDate day = LocalDate.now(ZoneOffset.UTC);
        String dedupe = day + ":" + userId + ":" + operation + ":" + operationKey;
        if (!deduplicatedOperations.add(dedupe)) return;
        String key = day + ":" + userId;
        int count = usage.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
        if (count > properties.getAiRequestsPerDay()) {
            deduplicatedOperations.remove(dedupe);
            usage.get(key).decrementAndGet();
            meters.counter("gamingplatform.rate_limit", "scope", "ai_daily").increment();
            throw new RateLimitException("Daily AI quota exceeded");
        }
        usage.keySet().removeIf(existing -> !existing.startsWith(day + ":"));
        deduplicatedOperations.removeIf(existing -> !existing.startsWith(day + ":"));
    }
}
