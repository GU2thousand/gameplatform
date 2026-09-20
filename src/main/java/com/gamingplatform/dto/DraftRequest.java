package com.gamingplatform.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record DraftRequest(@NotNull @Size(max = 10000) String answer) {}
