package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.mapper.FactoryMapper;
import com.baskiliisler.backend.model.Factory;
import com.baskiliisler.backend.repository.FactoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FactoryService {
    private final FactoryRepository factoryRepository;

    public Factory getFactoryById(Long factoryId) {
        return factoryRepository.findById(factoryId)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found"));
    }

    @Transactional
    public Factory create(FactoryRequestDto dto) {
        return factoryRepository.save(FactoryMapper.toEntity(dto));
    }

    @Transactional
    public Factory update(Long id, FactoryRequestDto dto) {
        Factory f = factoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found"));
        if (dto.name() != null)           f.setName(dto.name());
        if (dto.address() != null)        f.setAddress(dto.address());
        if (dto.dailyCapacity() != null)  f.setDailyCapacity(dto.dailyCapacity());
        if (dto.active() != null)         f.setActive(dto.active());
        return f;
    }

    @Transactional
    public List<FactoryResponseDto> list(boolean onlyActive) {
        return (onlyActive ? factoryRepository.findByActiveTrue()
                : factoryRepository.findAll())
                .stream().map(FactoryMapper::toDto).toList();
    }

    @Transactional
    public FactoryResponseDto get(Long id) {
        return FactoryMapper.toDto(factoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found")));
    }

    @Transactional
    public void delete(Long id) {
        factoryRepository.deleteById(id);
    }
}
