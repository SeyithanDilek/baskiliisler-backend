package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.mapper.BrandMapper;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandProcessService brandProcessService;
    private final BrandProcessHistoryService brandProcessHistoryService;
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Transactional
    public Brand createBrand(BrandRequestDto dto) {

        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı");
        }

        brandRepository.findByName(dto.name())
                .ifPresent(b -> { throw new IllegalArgumentException("Marka zaten var"); });

        Brand brand = brandRepository.save(BrandMapper.toEntity(dto));
        
        BrandProcess process = brandProcessService.createBrandProcess(brand);
        
        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                process,
                ProcessStatus.INIT,
                null,
                "{\"brandId\":" + brand.getId() + "}");

        return brand;
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public BrandDetailDto findById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        ProcessStatus status = brandProcessService.getProcessStatus(brand.getId());
        return BrandMapper.toDetailDto(brand, status);
    }

    @Transactional
    public BrandDetailDto updateBrand(Long id, BrandUpdateDto dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı");
        }

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));

        if (dto.name() != null && !dto.name().equals(brand.getName()) &&
                brandRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException("Bu isim zaten kullanımda");
        }

        BrandMapper.updateEntity(dto, brand);
        ProcessStatus status = brandProcessService.getProcessStatus(brand.getId());
        return BrandMapper.toDetailDto(brand, status);
    }

    @Transactional
    public void deleteBrand(Long id) {
        if (brandProcessService.existsBrandProcess(id)) {
            throw new IllegalStateException("Süreç devam ediyor, marka silinemez");
        }
        brandRepository.deleteById(id);
    }
}
