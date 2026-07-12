package com.gamingplatform.dto;

public record CsrfResponse(String token, String headerName, String parameterName) {}
