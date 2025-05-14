package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.ProcessStatus;

import java.time.LocalDateTime;

public record StatusResponseDto(
        ProcessStatus status,
        LocalDateTime updatedAt
) {}