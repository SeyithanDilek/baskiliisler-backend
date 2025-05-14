package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.mapper.BrandMapper;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.repository.BrandProcessRepository;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandProcessRepository processRepository;

    public Brand createBrand(BrandRequestDto dto) {
        brandRepository.findByName(dto.name()).ifPresent(b -> {
            throw new IllegalArgumentException("Bu marka zaten mevcut.");
        });

        Brand brand = BrandMapper.toEntity(dto);
        Brand saved = brandRepository.save(brand);

        createAndSaveBrandProcess(saved);

        return saved;
    }

    private void createAndSaveBrandProcess(Brand saved) {
        BrandProcess process = BrandProcess.builder()
                .brand(saved)
                .status(ProcessStatus.SAMPLE_LEFT)
                .updatedAt(LocalDateTime.now())
                .build();

        processRepository.save(process);
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public BrandDetailDto findById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        ProcessStatus status = processRepository.findByBrandId(id)
                .map(BrandProcess::getStatus)
                .orElse(null);
        return BrandMapper.toDetailDto(brand, status);
    }

    @Transactional
    public BrandDetailDto updateBrand(Long id, BrandUpdateDto dto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));

        if (dto.name() != null && !dto.name().equals(brand.getName()) &&
                brandRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException("Bu isim zaten kullanımda");
        }

        BrandMapper.updateEntity(dto, brand);
        ProcessStatus status = processRepository.findByBrandId(id)
                .map(BrandProcess::getStatus)
                .orElse(null);
        return BrandMapper.toDetailDto(brand, status);
    }

    @Transactional
    public void deleteBrand(Long id) {
        if (processRepository.existsByBrandId(id)) {
            throw new IllegalStateException("Süreç devam ediyor, marka silinemez");
        }
        brandRepository.deleteById(id);
    }
}
