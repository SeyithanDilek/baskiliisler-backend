package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record StatusUpdateRequestDto(
        @NotNull ProcessStatus newStatus,
        Map<String,Object> payload
) {}
