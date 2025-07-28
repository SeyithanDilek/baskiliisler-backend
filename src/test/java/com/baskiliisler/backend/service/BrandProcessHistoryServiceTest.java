package com.baskiliisler.backend.service;

import com.baskiliisler.backend.config.SecurityUtil;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandProcessHistoryServiceTest {

    @Mock
    private BrandProcessRepository processRepo;

    @Mock
    private ProcessHistoryRepository historyRepo;

    @Mock
    private BrandProcessService brandProcessService;

    @InjectMocks
    private BrandProcessHistoryService brandProcessHistoryService;

    private Brand testBrand;
    private BrandProcess testBrandProcess;
    private ProcessHistory testProcessHistory;
    private static final Long TEST_USER_ID = 100L;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        testBrandProcess = BrandProcess.builder()
                .id(1L)
                .brand(testBrand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .updatedAt(LocalDateTime.now())
                .build();

        testProcessHistory = ProcessHistory.builder()
                .id(1L)
                .process(testBrandProcess)
                .fromStatus(null)
                .toStatus(ProcessStatus.INIT)
                .actorId(TEST_USER_ID)
                .changedAt(LocalDateTime.now())
                .payload("{\"brandId\":" + testBrand.getId() + "}")
                .build();
    }

    @Test
    @DisplayName("Marka oluşturma için süreç geçmişi kaydetme")
    void whenSaveProcessHistoryForCreateBrand_thenSaveHistoryRecord() {
        // given
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(TEST_USER_ID);
            when(historyRepo.save(any(ProcessHistory.class))).thenReturn(testProcessHistory);

            // when
            brandProcessHistoryService.saveProcessHistoryForCreateBrand(testBrandProcess);

            // then
            verify(historyRepo).save(argThat(history -> 
                    history.getProcess().equals(testBrandProcess) &&
                    history.getFromStatus() == null &&
                    history.getToStatus().equals(ProcessStatus.INIT) &&
                    history.getActorId().equals(TEST_USER_ID) &&
                    history.getChangedAt() != null &&
                    history.getPayload().contains("\"brandId\":" + testBrand.getId())
            ));
        }
    }

    @Test
    @DisplayName("Durum değişikliği için süreç geçmişi kaydetme")
    void whenSaveProcessHistoryForChangeStatus_thenSaveHistoryRecord() {
        // given
        ProcessStatus fromStatus = ProcessStatus.SAMPLE_LEFT;
        ProcessStatus toStatus = ProcessStatus.OFFER_SENT;
        String customPayload = "{\"quoteId\":123}";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(TEST_USER_ID);
            when(historyRepo.save(any(ProcessHistory.class))).thenReturn(testProcessHistory);

            // when
            brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                    testBrandProcess, toStatus, fromStatus, customPayload);

            // then
            verify(historyRepo).save(argThat(history -> 
                    history.getProcess().equals(testBrandProcess) &&
                    history.getFromStatus().equals(fromStatus) &&
                    history.getToStatus().equals(toStatus) &&
                    history.getActorId().equals(TEST_USER_ID) &&
                    history.getChangedAt() != null &&
                    history.getPayload().equals(customPayload)
            ));
        }
    }

    @Test
    @DisplayName("Null payload ile durum değişikliği için süreç geçmişi kaydetme")
    void whenSaveProcessHistoryForChangeStatus_withNullPayload_thenSaveHistoryRecord() {
        // given
        ProcessStatus fromStatus = ProcessStatus.SAMPLE_LEFT;
        ProcessStatus toStatus = ProcessStatus.OFFER_SENT;
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(TEST_USER_ID);
            when(historyRepo.save(any(ProcessHistory.class))).thenReturn(testProcessHistory);

            // when
            brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                    testBrandProcess, toStatus, fromStatus, null);

            // then
            verify(historyRepo).save(argThat(history -> 
                    history.getProcess().equals(testBrandProcess) &&
                    history.getFromStatus().equals(fromStatus) &&
                    history.getToStatus().equals(toStatus) &&
                    history.getActorId().equals(TEST_USER_ID) &&
                    history.getChangedAt() != null &&
                    history.getPayload() == null
            ));
        }
    }

    @Test
    @DisplayName("Aynı durumdan aynı duruma geçiş için süreç geçmişi kaydetme")
    void whenSaveProcessHistoryForChangeStatus_withSameStatus_thenSaveHistoryRecord() {
        // given
        ProcessStatus sameStatus = ProcessStatus.OFFER_SENT;
        String revisionPayload = "Revizyon yapıldı. Teklif ID: 456";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(TEST_USER_ID);
            when(historyRepo.save(any(ProcessHistory.class))).thenReturn(testProcessHistory);

            // when
            brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                    testBrandProcess, sameStatus, sameStatus, revisionPayload);

            // then
            verify(historyRepo).save(argThat(history -> 
                    history.getProcess().equals(testBrandProcess) &&
                    history.getFromStatus().equals(sameStatus) &&
                    history.getToStatus().equals(sameStatus) &&
                    history.getActorId().equals(TEST_USER_ID) &&
                    history.getChangedAt() != null &&
                    history.getPayload().equals(revisionPayload)
            ));
        }
    }

    @Test
    @DisplayName("Farklı kullanıcı ile durum değişikliği için süreç geçmişi kaydetme")
    void whenSaveProcessHistoryForChangeStatus_withDifferentUser_thenSaveHistoryRecord() {
        // given
        Long differentUserId = 200L;
        ProcessStatus fromStatus = ProcessStatus.OFFER_SENT;
        ProcessStatus toStatus = ProcessStatus.ORDER_PLACED;
        String orderPayload = "Teklif kabul edildi. Sipariş ID: 789";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(differentUserId);
            when(historyRepo.save(any(ProcessHistory.class))).thenReturn(testProcessHistory);

            // when
            brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                    testBrandProcess, toStatus, fromStatus, orderPayload);

            // then
            verify(historyRepo).save(argThat(history -> 
                    history.getProcess().equals(testBrandProcess) &&
                    history.getFromStatus().equals(fromStatus) &&
                    history.getToStatus().equals(toStatus) &&
                    history.getActorId().equals(differentUserId) &&
                    history.getChangedAt() != null &&
                    history.getPayload().equals(orderPayload)
            ));
        }
    }
} 