package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.ProcessStatus;

import java.time.LocalDateTime;

public record ProcessHistoryDto(
        ProcessStatus from,
        ProcessStatus to,
        LocalDateTime at,
        Long actorId,
        String payloadJson
) {}
