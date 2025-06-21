package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateDto(
        @NotNull OrderStatus status
) {} 