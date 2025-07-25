package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.dto.UserUpdateDto;
import com.baskiliisler.backend.mapper.UserMapper;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.DealerRepository;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.util.PasswordGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final DealerRepository dealerRepository;

    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        return createUser(dto, Role.DEALER_USER);
    }

    @Transactional
    public UserResponseDto createUser(UserCreateDto dto, Role role) {
        return createUser(dto, role, null);
    }
    
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto, Role role, Long dealerId) {
        // Email uniqueness kontrolü
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }
        
        // 12 harfli şifre oluştur
        String plainPassword = PasswordGenerator.generatePassword();
        String passwordHash = passwordEncoder.encode(plainPassword);
        
        // Dealer'ı bul (eğer dealerId verilmişse)
        Dealer dealer = null;
        if (dealerId != null) {
            dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new IllegalArgumentException("Dealer bulunamadı: " + dealerId));
        }
        
        // Yeni kullanıcıyı belirtilen rol ile oluştur
        User user = UserMapper.toUser(dto, passwordHash, role, dealer);
        User savedUser = userRepository.save(user);
        
        // Hoşgeldiniz emaili gönder
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName(), plainPassword);
        
        return UserMapper.toResponseDto(savedUser);
    }

    public User getCurrentUser() {
        return userRepository.findById(SecurityUtil.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
        return UserMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
        
        // Email uniqueness kontrolü
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }
        
        UserMapper.updateUserFromDto(user, dto);
        User savedUser = userRepository.save(user);
        return UserMapper.toResponseDto(savedUser);
    }

    @Transactional
    public UserResponseDto updateCurrentUser(UserUpdateDto dto) {
        Long currentUserId = SecurityUtil.currentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
        
        // Email uniqueness kontrolü
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }
        
        // Kendi bilgilerini güncellerken role değiştiremez
        UserUpdateDto safeDto = new UserUpdateDto(dto.name(), dto.email(), dto.phoneNumber(), user.getRole());
        UserMapper.updateUserFromDto(user, safeDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toResponseDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        Long currentUserId = SecurityUtil.currentUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("Kendi hesabınızı silemezsiniz");
        }
        
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Kullanıcı bulunamadı");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
