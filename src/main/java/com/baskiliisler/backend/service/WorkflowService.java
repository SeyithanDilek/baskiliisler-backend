package com.baskiliisler.backend.service;

import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.CancelRequest;
import com.baskiliisler.backend.dto.ProcessHistoryDto;
import com.baskiliisler.backend.dto.StatusResponseDto;
import com.baskiliisler.backend.dto.StatusUpdateRequestDto;
import com.baskiliisler.backend.mapper.ProcessHistoryMapper;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.ProcessHistory;
import com.baskiliisler.backend.repository.BrandProcessRepository;
import com.baskiliisler.backend.repository.ProcessHistoryRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final BrandProcessRepository processRepo;
    private final ProcessHistoryRepository historyRepo;

    @Transactional
    public StatusResponseDto updateStatus(
            Long brandId,
            StatusUpdateRequestDto req) throws JsonProcessingException {
        Long actorId = SecurityUtil.currentUserId();

        BrandProcess process = processRepo.findByBrandIdForUpdate(brandId)
                .orElseThrow(() -> new EntityNotFoundException("Process not found"));

        ProcessStatus from = process.getStatus();
        ProcessStatus to   = req.newStatus();

        if (!process.getStatus().canTransitionTo(req.newStatus())) {
            throw new IllegalStateException(
                    "Geçersiz geçiş: " + process.getStatus() + " → " + req.newStatus());
        }

        process.setStatus(req.newStatus());
        process.setPayload(toJson(req.payload()));
        process.setUpdatedAt(LocalDateTime.now());

        historyRepo.save(ProcessHistory.builder()
                .process(process)
                .fromStatus(from)
                .toStatus(to)
                .actorId(actorId)
                .payload(process.getPayload())
                .changedAt(process.getUpdatedAt())
                .build());

       // events.publishEvent();

        return new StatusResponseDto(to, process.getUpdatedAt());
    }

    private String toJson(Map<String,Object> map) throws JsonProcessingException {
        return map == null ? null : new ObjectMapper().writeValueAsString(map);
    }

    @Transactional(readOnly = true)
    public List<ProcessHistoryDto> getHistory(Long brandId) {
        return historyRepo.findByBrandId(brandId)
                .stream().map(ProcessHistoryMapper::toDto).toList();
    }

    public void updateProcessStatus(Long brandId, ProcessStatus newStatus) {
        BrandProcess process = processRepo.findByBrandId(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Marka süreci bulunamadı"));

        process.setStatus(newStatus);
        process.setUpdatedAt(LocalDateTime.now());
        processRepo.save(process);
    }

    public ProcessStatus getProcessStatus(Long brandId) {
        return processRepo.findByBrandId(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Marka süreci bulunamadı"))
                .getStatus();
    }

    @Transactional
    public StatusResponseDto cancel(Long brandId, CancelRequest req) throws JsonProcessingException {
        StatusUpdateRequestDto statusReq = new StatusUpdateRequestDto(
                ProcessStatus.CANCELLED,
                Map.of("reason", req.reason())
        );
        return updateStatus(brandId, statusReq);
    }

}
