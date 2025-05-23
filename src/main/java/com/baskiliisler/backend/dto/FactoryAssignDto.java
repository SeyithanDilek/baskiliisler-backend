package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FactoryAssignDto(
        @NotNull Long factoryId,
        LocalDate deadline
) {}
