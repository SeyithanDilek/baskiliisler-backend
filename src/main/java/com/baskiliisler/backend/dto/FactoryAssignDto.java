package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record FactoryAssignDto(
        @NotNull Long factoryId,
        LocalDate deadline,
        String description,
        List<String> imageUrls
) {}
