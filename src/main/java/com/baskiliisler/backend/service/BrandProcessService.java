package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.repository.BrandProcessRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BrandProcessService {
    BrandProcessRepository brandProcessRepository;

    public BrandProcess createBrandProcess(Brand brand) {
        BrandProcess brandProcess = BrandProcess.builder()
                .brand(brand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .updatedAt(LocalDateTime.now())
                .build();
        return brandProcessRepository.save(brandProcess);
    }

    public BrandProcess updateBrandProcessStatus(Long brandId, ProcessStatus status) {
        BrandProcess brandProcess = brandProcessRepository.findByBrandIdForUpdate(brandId)
                .orElseThrow();
        brandProcess.setStatus(status);
        brandProcess.setUpdatedAt(LocalDateTime.now());
        return brandProcess;
    }

    public BrandProcess checkForExpired(Long brandId) {
        BrandProcess brandProcess = brandProcessRepository.findByBrandIdForUpdate(brandId).get();
        if (brandProcess.getStatus() == ProcessStatus.OFFER_SENT) {
            brandProcess.setStatus(ProcessStatus.EXPIRED);
            brandProcess.setUpdatedAt(LocalDateTime.now());
        }
        return brandProcess;
    }

    public BrandProcess getBrandProcess(Long brandId) {
        return brandProcessRepository.findByBrandId(brandId)
                .orElseThrow(() -> new EntityNotFoundException("Brand process not found"));
    }

    public ProcessStatus getProcessStatus(Long brandId) {
        return brandProcessRepository.findByBrandId(brandId)
                .map(BrandProcess::getStatus)
                .orElse(null);
    }

    public boolean existsBrandProcess(Long brandId) {
        return brandProcessRepository.existsByBrandId(brandId);
    }



}
