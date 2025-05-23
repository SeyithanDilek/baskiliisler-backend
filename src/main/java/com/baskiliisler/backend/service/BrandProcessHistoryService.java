package com.baskiliisler.backend.service;

import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.repository.BrandProcessRepository;
import com.baskiliisler.backend.repository.ProcessHistoryRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BrandProcessHistoryService {

    private final BrandProcessRepository processRepo;
    private final ProcessHistoryRepository historyRepo;
    private final BrandProcessService brandProcessService;

    @Transactional
    public void saveProcessHistoryForCreateBrand(BrandProcess bp) {
        historyRepo.save(
                ProcessHistory.builder()
                        .process(bp)
                        .fromStatus(null)
                        .toStatus(ProcessStatus.INIT)
                        .actorId(SecurityUtil.currentUserId())
                        .changedAt(LocalDateTime.now())
                        .payload("{\"brandId\":" + bp.getBrand().getId() + "}")
                        .build());
    }


    @Transactional
    public void saveProcessHistoryForChangeStatus(BrandProcess bp,
                                                  ProcessStatus toStatus,
                                                  ProcessStatus fromStatus,
                                                  String payload) {
        historyRepo.save(ProcessHistory.builder()
                .process(bp)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .actorId(SecurityUtil.currentUserId())
                .changedAt(LocalDateTime.now())
                .payload(payload)
                .build());
    }

}
