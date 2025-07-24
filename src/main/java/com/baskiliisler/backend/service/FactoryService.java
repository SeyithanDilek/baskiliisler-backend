package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.mapper.FactoryMapper;
import com.baskiliisler.backend.model.Factory;
import com.baskiliisler.backend.repository.FactoryRepository;
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

    @Transactional
    public Factory create(FactoryRequestDto dto) {
        // 1. Önce kullanıcı oluşturmayı dene (eğer hata varsa hiçbir şey kaydedilmez)
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name(dto.name() + " Kullanıcısı")  // Fabrika adı + "Kullanıcısı"
                .email(dto.userEmail())
                .phoneNumber("+90 555 000 00 00")  // Varsayılan telefon
                .build();
        
        // Kullanıcı oluşturma işlemi (hata varsa exception fırlatır ve hiçbir şey kaydedilmez)
        userService.createUser(userCreateDto, Role.FACTORY_USER);
        log.info("Fabrika kullanıcısı oluşturuldu: {} (Fabrika: {})", dto.userEmail(), dto.name());
        
        // 2. Kullanıcı başarıyla oluşturulduysa fabrikayı oluştur
        Factory factory = factoryRepository.save(FactoryMapper.toEntity(dto));
        log.info("Fabrika oluşturuldu: {} (ID: {})", factory.getName(), factory.getId());
        
        return factory;
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
