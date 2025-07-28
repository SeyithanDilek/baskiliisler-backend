package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.ProcessHistoryDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.ProcessHistory;
import com.baskiliisler.backend.type.ProcessStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessHistoryMapper Test")
class ProcessHistoryMapperTest {

    private ProcessHistory processHistoryWithFromStatus;
    private ProcessHistory processHistoryWithoutFromStatus;
    private BrandProcess testBrandProcess;

    @BeforeEach
    void setUp() {
        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        testBrandProcess = BrandProcess.builder()
                .id(1L)
                .brand(testBrand)
                .status(ProcessStatus.OFFER_SENT)
                .updatedAt(LocalDateTime.now())
                .build();

        processHistoryWithFromStatus = ProcessHistory.builder()
                .id(1L)
                .process(testBrandProcess)
                .fromStatus(ProcessStatus.SAMPLE_LEFT)
                .toStatus(ProcessStatus.OFFER_SENT)
                .actorId(100L)
                .changedAt(LocalDateTime.now().minusHours(2))
                .payload("{\"quoteId\":123}")
                .build();

        processHistoryWithoutFromStatus = ProcessHistory.builder()
                .id(2L)
                .process(testBrandProcess)
                .fromStatus(null)  // İlk süreç kaydı
                .toStatus(ProcessStatus.INIT)
                .actorId(200L)
                .changedAt(LocalDateTime.now().minusDays(1))
                .payload("{\"brandId\":1}")
                .build();
    }

    @Test
    @DisplayName("fromStatus'u olan süreç geçmişini DTO'ya dönüştürme")
    void whenToDto_withFromStatus_thenReturnCompleteProcessHistoryDto() {
        // when
        ProcessHistoryDto result = ProcessHistoryMapper.toDto(processHistoryWithFromStatus);

        // then
        assertThat(result).isNotNull();
        assertThat(result.from()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
        assertThat(result.to()).isEqualTo(ProcessStatus.OFFER_SENT);
        assertThat(result.at()).isEqualTo(processHistoryWithFromStatus.getChangedAt());
        assertThat(result.actorId()).isEqualTo(100L);
        assertThat(result.payloadJson()).isEqualTo("{\"quoteId\":123}");
    }

    @Test
    @DisplayName("fromStatus'u null olan süreç geçmişini DTO'ya dönüştürme")
    void whenToDto_withNullFromStatus_thenReturnProcessHistoryDtoWithNullFrom() {
        // when
        ProcessHistoryDto result = ProcessHistoryMapper.toDto(processHistoryWithoutFromStatus);

        // then
        assertThat(result).isNotNull();
        assertThat(result.from()).isNull();  // İlk süreç kaydı
        assertThat(result.to()).isEqualTo(ProcessStatus.INIT);
        assertThat(result.at()).isEqualTo(processHistoryWithoutFromStatus.getChangedAt());
        assertThat(result.actorId()).isEqualTo(200L);
        assertThat(result.payloadJson()).isEqualTo("{\"brandId\":1}");
    }

    @Test
    @DisplayName("Null payload olan süreç geçmişini DTO'ya dönüştürme")
    void whenToDto_withNullPayload_thenReturnProcessHistoryDtoWithNullPayload() {
        // given
        ProcessHistory historyWithNullPayload = ProcessHistory.builder()
                .id(3L)
                .process(testBrandProcess)
                .fromStatus(ProcessStatus.OFFER_SENT)
                .toStatus(ProcessStatus.ORDER_PLACED)
                .actorId(300L)
                .changedAt(LocalDateTime.now())
                .payload(null)
                .build();

        // when
        ProcessHistoryDto result = ProcessHistoryMapper.toDto(historyWithNullPayload);

        // then
        assertThat(result).isNotNull();
        assertThat(result.from()).isEqualTo(ProcessStatus.OFFER_SENT);
        assertThat(result.to()).isEqualTo(ProcessStatus.ORDER_PLACED);
        assertThat(result.at()).isEqualTo(historyWithNullPayload.getChangedAt());
        assertThat(result.actorId()).isEqualTo(300L);
        assertThat(result.payloadJson()).isNull();
    }

    @Test
    @DisplayName("CANCELLED durumuna geçiş süreç geçmişini DTO'ya dönüştürme")
    void whenToDto_withCancelledTransition_thenReturnCorrectDto() {
        // given
        ProcessHistory cancelledHistory = ProcessHistory.builder()
                .id(4L)
                .process(testBrandProcess)
                .fromStatus(ProcessStatus.OFFER_SENT)
                .toStatus(ProcessStatus.CANCELLED)
                .actorId(400L)
                .changedAt(LocalDateTime.now())
                .payload("{\"reason\":\"Müşteri isteği\",\"cancelledBy\":\"admin\"}")
                .build();

        // when
        ProcessHistoryDto result = ProcessHistoryMapper.toDto(cancelledHistory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.from()).isEqualTo(ProcessStatus.OFFER_SENT);
        assertThat(result.to()).isEqualTo(ProcessStatus.CANCELLED);
        assertThat(result.at()).isEqualTo(cancelledHistory.getChangedAt());
        assertThat(result.actorId()).isEqualTo(400L);
        assertThat(result.payloadJson()).contains("Müşteri isteği");
        assertThat(result.payloadJson()).contains("admin");
    }

    @Test
    @DisplayName("COMPLETED durumuna geçiş süreç geçmişini DTO'ya dönüştürme")
    void whenToDto_withCompletedTransition_thenReturnCorrectDto() {
        // given
        ProcessHistory completedHistory = ProcessHistory.builder()
                .id(5L)
                .process(testBrandProcess)
                .fromStatus(ProcessStatus.SENT_TO_FACTORY)
                .toStatus(ProcessStatus.COMPLETED)
                .actorId(500L)
                .changedAt(LocalDateTime.now())
                .payload("{\"orderId\":789,\"deliveredAt\":\"2024-01-15T10:30:00\"}")
                .build();

        // when
        ProcessHistoryDto result = ProcessHistoryMapper.toDto(completedHistory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.from()).isEqualTo(ProcessStatus.SENT_TO_FACTORY);
        assertThat(result.to()).isEqualTo(ProcessStatus.COMPLETED);
        assertThat(result.at()).isEqualTo(completedHistory.getChangedAt());
        assertThat(result.actorId()).isEqualTo(500L);
        assertThat(result.payloadJson()).contains("orderId");
        assertThat(result.payloadJson()).contains("789");
        assertThat(result.payloadJson()).contains("deliveredAt");
    }

    @Test
    @DisplayName("EXPIRED durumuna geçiş süreç geçmişini DTO'ya dönüştürme")
    void whenToDto_withExpiredTransition_thenReturnCorrectDto() {
        // given
        ProcessHistory expiredHistory = ProcessHistory.builder()
                .id(6L)
                .process(testBrandProcess)
                .fromStatus(ProcessStatus.OFFER_SENT)
                .toStatus(ProcessStatus.EXPIRED)
                .actorId(600L)
                .changedAt(LocalDateTime.now())
                .payload("{\"quoteId\":456,\"expiredAt\":\"2024-01-10T00:00:00\"}")
                .build();

        // when
        ProcessHistoryDto result = ProcessHistoryMapper.toDto(expiredHistory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.from()).isEqualTo(ProcessStatus.OFFER_SENT);
        assertThat(result.to()).isEqualTo(ProcessStatus.EXPIRED);
        assertThat(result.at()).isEqualTo(expiredHistory.getChangedAt());
        assertThat(result.actorId()).isEqualTo(600L);
        assertThat(result.payloadJson()).contains("quoteId");
        assertThat(result.payloadJson()).contains("456");
        assertThat(result.payloadJson()).contains("expiredAt");
    }
} 