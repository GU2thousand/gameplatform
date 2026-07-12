package com.gamingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[^\\p{Cc}\\p{Cf}]+$", message = "must not contain control characters")
    private String username;

    @NotBlank
    @Size(min = 10, max = 128)
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username == null ? null : username.trim(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
