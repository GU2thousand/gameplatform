package com.gamingplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {
    private String legacyClaimSecret = "";

    public String getLegacyClaimSecret() { return legacyClaimSecret; }
    public void setLegacyClaimSecret(String legacyClaimSecret) {
        this.legacyClaimSecret = legacyClaimSecret == null ? "" : legacyClaimSecret;
    }
}
