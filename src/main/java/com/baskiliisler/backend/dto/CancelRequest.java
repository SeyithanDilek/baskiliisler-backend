package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelRequest(
        @NotBlank String reason
) {}
