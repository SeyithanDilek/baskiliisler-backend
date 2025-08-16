package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.dto.UserUpdateDto;
import com.baskiliisler.backend.mapper.UserMapper;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.Factory;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.repository.DealerRepository;
import com.baskiliisler.backend.repository.FactoryRepository;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final FactoryRepository factoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        return createUser(dto, Role.DEALER_USER);
    }

    @Transactional
    public UserResponseDto createUser(UserCreateDto dto, Role role) {
        // Email uniqueness kontrolü
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }
        
        // 12 harfli şifre oluştur
        String plainPassword = PasswordGenerator.generatePassword();
        String passwordHash = passwordEncoder.encode(plainPassword);
        
        // Yeni kullanıcıyı belirtilen rol ile oluştur
        User user = UserMapper.toUser(dto, passwordHash, role);
        
        // Dealer ataması - FACTORY_USER için dealer gerekli değil
        if (role != Role.FACTORY_USER) {
            User currentUser = getCurrentUser();
            if (dto.getDealerId() == null) {
                // dealerId null ise kullanıcının dealer'ını kullan
                if (currentUser.getDealer() == null) {
                    throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
                }
                user.setDealer(currentUser.getDealer());
            } else {
                // dealerId verilmişse, sadece SUPER_ADMIN farklı dealer seçebilir
                if (currentUser.getRole() == Role.SUPER_ADMIN) {
                    Dealer dealer = dealerRepository.findById(dto.getDealerId())
                            .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
                    user.setDealer(dealer);
                } else {
                    // Diğer roller sadece kendi dealer'larını seçebilir
                    if (currentUser.getDealer() == null) {
                        throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
                    }
                    if (!dto.getDealerId().equals(currentUser.getDealer().getId())) {
                        throw new IllegalStateException("Sadece kendi dealer'ınızı seçebilirsiniz");
                    }
                    user.setDealer(currentUser.getDealer());
                }
            }
        }
        
        // Factory ataması - FACTORY_USER için factory gerekli
        if (role == Role.FACTORY_USER && dto.getFactoryId() != null) {
            Factory factory = factoryRepository.findById(dto.getFactoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Factory bulunamadı"));
            user.setFactory(factory);
        }
        
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

    public List<User> getUsersByDealer(Long dealerId) {
        User currentUser = getCurrentUser();
        
        // Yetki kontrolü
        if (currentUser.getRole() != Role.SUPER_ADMIN && 
            (currentUser.getDealer() == null || !dealerId.equals(currentUser.getDealer().getId()))) {
            throw new IllegalStateException("Bu dealer'ın verilerine erişim yetkiniz yok");
        }
        
        // Dealer'ı bul
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
        
        return userRepository.findByDealer(dealer);
    }

    public List<User> getDealerUsers() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() != Role.DEALER_ADMIN) {
            throw new IllegalStateException("Bu endpoint sadece DEALER_ADMIN kullanıcıları tarafından kullanılabilir");
        }
        
        if (currentUser.getDealer() == null) {
            throw new IllegalStateException("Kullanıcının atanmış bir bayisi yok");
        }
        
        return userRepository.findByDealer(currentUser.getDealer());
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

    public Long getCurrentUserFactoryId() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.FACTORY_USER) {
            throw new IllegalStateException("Bu işlem sadece FACTORY_USER rolü için geçerlidir");
        }
        
        // JWT claim'den factoryId'yi al (daha performanslı)
        Long factoryId = SecurityUtil.currentUserFactoryId();
        
        // Backwards compatibility: Eski token'larda factoryId claim'i yoksa database'den al
        if (factoryId == null) {
            if (currentUser.getFactory() == null) {
                throw new IllegalStateException("FACTORY_USER kullanıcısının atanmış bir fabrikası yok");
            }
            factoryId = currentUser.getFactory().getId();
        }
        
        return factoryId;
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
