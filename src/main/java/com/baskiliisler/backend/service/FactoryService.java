package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.dto.FactoryCreateDto;
import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.mapper.FactoryMapper;
import com.baskiliisler.backend.model.Factory;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.repository.FactoryRepository;
import com.baskiliisler.backend.repository.DealerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FactoryService {
    private final FactoryRepository factoryRepository;
    private final UserService userService;

    public Factory getFactoryById(Long factoryId) {
        return factoryRepository.findById(factoryId)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found"));
    }

    public Factory getFactoryByName(String factoryName) {
        return factoryRepository.findByName(factoryName)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found with name: " + factoryName));
    }



    @Transactional
    public Factory createWithUser(FactoryCreateDto dto) {
        // 1. Önce fabrikayı oluştur
        Factory factory = Factory.builder()
                .name(dto.factory().name())
                .address(dto.factory().address())
                .factoryNumber(dto.factory().factoryNumber())
                .active(true)
                .build();
        
        Factory savedFactory = factoryRepository.save(factory);
        log.info("Fabrika oluşturuldu: {} (ID: {})", savedFactory.getName(), savedFactory.getId());
        
        // 2. Fabrika kullanıcısını oluştur
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name(dto.user().name())
                .email(dto.user().email())
                .phoneNumber(dto.user().phoneNumber())
                .factoryId(savedFactory.getId()) // Oluşturulan factory'nin ID'sini set et
                .build();
        
        // Kullanıcı oluşturma işlemi (hata varsa exception fırlatır ve rollback olur)
        userService.createUser(userCreateDto, Role.FACTORY_USER);
        log.info("Fabrika kullanıcısı oluşturuldu: {} (Fabrika: {})", dto.user().email(), savedFactory.getName());
        
        return savedFactory;
    }

    @Transactional
    public Factory update(Long id, FactoryRequestDto dto) {
        Factory f = factoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factory not found"));
        if (dto.name() != null)           f.setName(dto.name());
        if (dto.address() != null)        f.setAddress(dto.address());
        if (dto.active() != null)         f.setActive(dto.active());
        if (dto.phoneNumber() != null)    f.setPhoneNumber(dto.phoneNumber());
        if (dto.factoryNumber() != null)  f.setFactoryNumber(dto.factoryNumber());
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
