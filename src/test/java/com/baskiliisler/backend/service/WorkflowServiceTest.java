package com.baskiliisler.backend.service;

import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.CancelRequest;
import com.baskiliisler.backend.dto.ProcessHistoryDto;
import com.baskiliisler.backend.dto.StatusResponseDto;
import com.baskiliisler.backend.dto.StatusUpdateRequestDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.ProcessHistory;
import com.baskiliisler.backend.repository.BrandProcessRepository;
import com.baskiliisler.backend.repository.ProcessHistoryRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private BrandProcessRepository processRepo;

    @Mock
    private ProcessHistoryRepository historyRepo;

    @InjectMocks
    private WorkflowService workflowService;

    private Brand testBrand;
    private BrandProcess testProcess;
    private ProcessHistory testHistory;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .build();

        testProcess = BrandProcess.builder()
                .id(1L)
                .brand(testBrand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .updatedAt(LocalDateTime.now())
                .build();

        testHistory = ProcessHistory.builder()
                .process(testProcess)
                .fromStatus(ProcessStatus.SAMPLE_LEFT)
                .toStatus(ProcessStatus.OFFER_SENT)
                .actorId(1L)
                .changedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Süreç durumu güncellendiğinde")
    void whenUpdateStatus_thenUpdateProcessAndCreateHistory() throws Exception {
        // given
        Long brandId = 1L;
        Long actorId = 1L;
        StatusUpdateRequestDto request = new StatusUpdateRequestDto(
                ProcessStatus.OFFER_SENT,
                Map.of("note", "Test note")
        );

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::currentUserId).thenReturn(actorId);
            when(processRepo.findByBrandIdForUpdate(brandId)).thenReturn(Optional.of(testProcess));
            when(historyRepo.save(any(ProcessHistory.class))).thenAnswer(i -> i.getArgument(0));

            // when
            StatusResponseDto response = workflowService.updateStatus(brandId, request);

            // then
            assertThat(response.status()).isEqualTo(ProcessStatus.OFFER_SENT);
            assertThat(response.updatedAt()).isNotNull();

            ArgumentCaptor<ProcessHistory> historyCaptor = ArgumentCaptor.forClass(ProcessHistory.class);
            verify(historyRepo).save(historyCaptor.capture());

            ProcessHistory savedHistory = historyCaptor.getValue();
            assertThat(savedHistory.getFromStatus()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
            assertThat(savedHistory.getToStatus()).isEqualTo(ProcessStatus.OFFER_SENT);
            assertThat(savedHistory.getActorId()).isEqualTo(actorId);
        }
    }

    @Test
    @DisplayName("Süreç geçmişi listelendiğinde")
    void whenGetHistory_thenReturnHistoryList() {
        // given
        Long brandId = 1L;
        when(historyRepo.findByBrandId(brandId)).thenReturn(List.of(testHistory));

        // when
        List<ProcessHistoryDto> historyList = workflowService.getHistory(brandId);

        // then
        assertThat(historyList)
                .isNotEmpty()
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    assertThat(dto.from()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
                    assertThat(dto.to()).isEqualTo(ProcessStatus.OFFER_SENT);
                    assertThat(dto.actorId()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("Geçersiz durum geçişi denendiğinde")
    void whenInvalidStatusTransition_thenThrowException() {
        // given
        Long brandId = 1L;
        Long actorId = 1L;
        StatusUpdateRequestDto request = new StatusUpdateRequestDto(
                ProcessStatus.DELIVERED,
                Map.of("note", "Invalid transition")
        );

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::currentUserId).thenReturn(actorId);
            when(processRepo.findByBrandIdForUpdate(brandId)).thenReturn(Optional.of(testProcess));

            // when & then
            assertThatThrownBy(() -> workflowService.updateStatus(brandId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Geçersiz geçiş");

            verify(historyRepo, never()).save(any());
        }
    }

    @Test
    @DisplayName("İptal isteği yapıldığında")
    void whenCancel_thenUpdateStatusToCancelled() throws Exception {
        // given
        Long brandId = 1L;
        Long actorId = 1L;
        CancelRequest request = new CancelRequest("Test iptal nedeni");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::currentUserId).thenReturn(actorId);
            when(processRepo.findByBrandIdForUpdate(brandId)).thenReturn(Optional.of(testProcess));
            when(historyRepo.save(any(ProcessHistory.class))).thenAnswer(i -> i.getArgument(0));

            // when
            StatusResponseDto response = workflowService.cancel(brandId, request);

            // then
            assertThat(response.status()).isEqualTo(ProcessStatus.CANCELLED);
            assertThat(response.updatedAt()).isNotNull();

            ArgumentCaptor<ProcessHistory> historyCaptor = ArgumentCaptor.forClass(ProcessHistory.class);
            verify(historyRepo).save(historyCaptor.capture());

            ProcessHistory savedHistory = historyCaptor.getValue();
            assertThat(savedHistory.getFromStatus()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
            assertThat(savedHistory.getToStatus()).isEqualTo(ProcessStatus.CANCELLED);
            assertThat(savedHistory.getActorId()).isEqualTo(actorId);
        }
    }

    @Test
    @DisplayName("Süreç durumu alındığında")
    void whenGetProcessStatus_thenReturnStatus() {
        // given
        Long brandId = 1L;
        when(processRepo.findByBrandId(brandId)).thenReturn(Optional.of(testProcess));

        // when
        ProcessStatus status = workflowService.getProcessStatus(brandId);

        // then
        assertThat(status).isEqualTo(ProcessStatus.SAMPLE_LEFT);
    }

    @Test
    @DisplayName("Olmayan süreç durumu alınmaya çalışıldığında")
    void whenGetProcessStatus_withNonExistingId_thenThrowException() {
        // given
        Long brandId = 999L;
        when(processRepo.findByBrandId(brandId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workflowService.getProcessStatus(brandId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Marka süreci bulunamadı");
    }
} 