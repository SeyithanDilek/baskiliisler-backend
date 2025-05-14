package com.baskiliisler.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baskiliisler.backend.config.TestSecurityConfig;
import com.baskiliisler.backend.dto.CancelRequest;
import com.baskiliisler.backend.dto.ProcessHistoryDto;
import com.baskiliisler.backend.dto.StatusResponseDto;
import com.baskiliisler.backend.dto.StatusUpdateRequestDto;
import com.baskiliisler.backend.service.WorkflowService;
import com.baskiliisler.backend.type.ProcessStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(WorkflowController.class)
@Import(TestSecurityConfig.class)
class WorkflowControllerTest {

    @InjectMocks
    private WorkflowController workflowController;

    @Mock
    private WorkflowService workflowService;

    @Captor
    private ArgumentCaptor<StatusUpdateRequestDto> statusUpdateCaptor;

    @Captor
    private ArgumentCaptor<CancelRequest> cancelRequestCaptor;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(workflowController)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Süreç durumu güncellendiğinde başarılı yanıt dönmeli")
    void whenUpdateStatus_thenReturnSuccess() throws Exception {
        // given
        Long brandId = 1L;
        StatusUpdateRequestDto request = new StatusUpdateRequestDto(
                ProcessStatus.OFFER_SENT,
                Map.of("note", "Test notu")
        );

        LocalDateTime now = LocalDateTime.now();
        StatusResponseDto response = new StatusResponseDto(ProcessStatus.OFFER_SENT, now);

        when(workflowService.updateStatus(eq(brandId), any(StatusUpdateRequestDto.class)))
                .thenReturn(response);

        // when
        mockMvc.perform(patch("/workflow/brands/{id}/status", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(response.status().name()))
                .andExpect(jsonPath("$.updatedAt").exists());

        // then
        verify(workflowService, times(1)).updateStatus(eq(brandId), statusUpdateCaptor.capture());
        StatusUpdateRequestDto capturedRequest = statusUpdateCaptor.getValue();
        assertThat(capturedRequest.newStatus()).isEqualTo(ProcessStatus.OFFER_SENT);
        assertThat(capturedRequest.payload()).containsEntry("note", "Test notu");
    }

    @Test
    @WithMockUser(roles = "REP")
    @DisplayName("Süreç geçmişi listelendiğinde tüm kayıtlar dönmeli")
    void whenGetHistory_thenReturnAllRecords() throws Exception {
        // given
        Long brandId = 1L;
        ProcessHistoryDto historyDto = new ProcessHistoryDto(
                ProcessStatus.SAMPLE_LEFT,
                ProcessStatus.OFFER_SENT,
                LocalDateTime.now(),
                1L,
                "{\"note\":\"Test notu\"}"
        );

        when(workflowService.getHistory(brandId)).thenReturn(List.of(historyDto));

        // when & then
        mockMvc.perform(get("/workflow/brands/{id}/history", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].from").value(historyDto.from().name()))
                .andExpect(jsonPath("$[0].to").value(historyDto.to().name()))
                .andExpect(jsonPath("$[0].actorId").value(historyDto.actorId()))
                .andExpect(jsonPath("$[0].payloadJson").value(historyDto.payloadJson()));

        verify(workflowService, times(1)).getHistory(brandId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("İptal isteği yapıldığında başarılı yanıt dönmeli")
    void whenCancel_thenReturnSuccess() throws Exception {
        // given
        Long brandId = 1L;
        CancelRequest request = new CancelRequest("Test iptal nedeni");
        LocalDateTime now = LocalDateTime.now();
        StatusResponseDto response = new StatusResponseDto(ProcessStatus.CANCELLED, now);

        when(workflowService.cancel(eq(brandId), any(CancelRequest.class)))
                .thenReturn(response);

        // when
        mockMvc.perform(patch("/workflow/brands/{id}/cancel", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(response.status().name()))
                .andExpect(jsonPath("$.updatedAt").exists());

        // then
        verify(workflowService, times(1)).cancel(eq(brandId), cancelRequestCaptor.capture());
        CancelRequest capturedRequest = cancelRequestCaptor.getValue();
        assertThat(capturedRequest.reason()).isEqualTo("Test iptal nedeni");
    }

    @Test
    @DisplayName("Kimlik doğrulaması olmadan istek yapıldığında 401 dönmeli")
    void whenUnauthenticatedRequest_thenReturn401() throws Exception {
        mockMvc.perform(get("/workflow/brands/1/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Yetkisiz kullanıcı istek yaptığında 403 dönmeli")
    void whenUnauthorizedRequest_thenReturn403() throws Exception {
        mockMvc.perform(patch("/workflow/brands/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "newStatus": "OFFER_SENT",
                            "payload": {"note": "Test notu"}
                        }
                        """))
                .andExpect(status().isForbidden());
    }
} 