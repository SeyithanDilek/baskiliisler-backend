package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.ProcessHistoryDto;
import com.baskiliisler.backend.model.ProcessHistory;

public final class ProcessHistoryMapper {
    public static ProcessHistoryDto toDto(ProcessHistory h) {
        return new ProcessHistoryDto(
                h.getFromStatus(),
                h.getToStatus(),
                h.getChangedAt(),
                h.getActorId(),
                h.getPayload());
    }
}