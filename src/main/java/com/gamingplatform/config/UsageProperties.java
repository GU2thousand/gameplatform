package com.gamingplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.usage")
public class UsageProperties {
    private int requestsPerMinute = 120;
    private int aiRequestsPerDay = 50;

    public int getRequestsPerMinute() { return requestsPerMinute; }
    public void setRequestsPerMinute(int value) { requestsPerMinute = Math.max(1, value); }
    public int getAiRequestsPerDay() { return aiRequestsPerDay; }
    public void setAiRequestsPerDay(int value) { aiRequestsPerDay = Math.max(1, value); }
}
