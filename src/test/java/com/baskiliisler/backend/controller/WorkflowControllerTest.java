package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.CancelRequest;
import com.baskiliisler.backend.dto.ProcessHistoryDto;
import com.baskiliisler.backend.dto.StatusResponseDto;
import com.baskiliisler.backend.dto.StatusUpdateRequestDto;
import com.baskiliisler.backend.service.WorkflowService;
import com.baskiliisler.backend.type.ProcessStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkflowControllerTest {

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private WorkflowController workflowController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workflowController).build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Marka durumu güncellendiğinde")
    void whenUpdateStatus_thenReturnUpdatedStatus() throws Exception {
        // given
        Long brandId = 1L;
        StatusUpdateRequestDto request = new StatusUpdateRequestDto(
                ProcessStatus.SAMPLE_LEFT,
                Map.of("note", "Test note")
        );

        StatusResponseDto response = new StatusResponseDto(
                ProcessStatus.SAMPLE_LEFT,
                LocalDateTime.now()
        );

        when(workflowService.updateStatus(eq(brandId), any(StatusUpdateRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(patch("/workflow/brands/{id}/status", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(response.status().name()))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Marka geçmişi getirildiğinde")
    void whenGetHistory_thenReturnHistoryList() throws Exception {
        // given
        Long brandId = 1L;
        ProcessHistoryDto history = new ProcessHistoryDto(
                ProcessStatus.INIT,
                ProcessStatus.SAMPLE_LEFT,
                LocalDateTime.now(),
                1L,
                "{\"note\":\"Test note\"}"
        );

        when(workflowService.getHistory(brandId)).thenReturn(List.of(history));

        // when & then
        mockMvc.perform(get("/workflow/brands/{id}/history", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].from").value(history.from().name()))
                .andExpect(jsonPath("$[0].to").value(history.to().name()))
                .andExpect(jsonPath("$[0].actorId").value(history.actorId()))
                .andExpect(jsonPath("$[0].payloadJson").value(history.payloadJson()))
                .andExpect(jsonPath("$[0].at").exists());
    }

    @Test
    @DisplayName("Marka iptal edildiğinde")
    void whenCancelBrand_thenReturnCancelledStatus() throws Exception {
        // given
        Long brandId = 1L;
        CancelRequest request = new CancelRequest("Test reason");

        StatusResponseDto response = new StatusResponseDto(
                ProcessStatus.CANCELLED,
                LocalDateTime.now()
        );

        when(workflowService.cancel(eq(brandId), any(CancelRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(patch("/workflow/brands/{id}/cancel", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(response.status().name()))
                .andExpect(jsonPath("$.updatedAt").exists());
    }
} 