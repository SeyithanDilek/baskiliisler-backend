package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.CancelRequest;
import com.baskiliisler.backend.dto.ProcessHistoryDto;
import com.baskiliisler.backend.dto.StatusResponseDto;
import com.baskiliisler.backend.dto.StatusUpdateRequestDto;
import com.baskiliisler.backend.service.WorkflowService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PatchMapping("/brands/{id}/status")
    public StatusResponseDto updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody StatusUpdateRequestDto request) throws JsonProcessingException {

        return workflowService.updateStatus(id, request);
    }

    @GetMapping("/brands/{id}/history")
    public List<ProcessHistoryDto> history(@PathVariable Long id) {
        return workflowService.getHistory(id);
    }

    @PatchMapping("/brands/{id}/cancel")
    public StatusResponseDto cancel(@PathVariable Long id,
                                 @Valid @RequestBody CancelRequest req) throws JsonProcessingException {
        return workflowService.cancel(id, req);
    }
}