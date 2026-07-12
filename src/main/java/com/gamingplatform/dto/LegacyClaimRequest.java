package com.gamingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LegacyClaimRequest {
    @NotBlank @Size(max = 100)
    private String username;
    @NotBlank @Size(min = 10, max = 128)
    private String newPassword;
    @NotBlank @Size(max = 256)
    private String claimSecret;

    public String getUsername() { return username; }
    public void setUsername(String value) { username = value == null ? null : value.trim(); }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String value) { newPassword = value; }
    public String getClaimSecret() { return claimSecret; }
    public void setClaimSecret(String value) { claimSecret = value; }
}
