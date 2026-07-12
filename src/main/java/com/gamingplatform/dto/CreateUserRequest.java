package com.gamingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[^\\p{Cc}\\p{Cf}]+$", message = "must not contain control characters")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }
}
