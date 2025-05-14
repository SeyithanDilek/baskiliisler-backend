package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.ProcessStatus;

public record BrandDetailDto(      Long id,
                                   String name,
                                   String contactEmail,
                                   String contactPhone,
                                   ProcessStatus status
) {}