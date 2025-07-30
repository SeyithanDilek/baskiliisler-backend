package com.baskiliisler.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.login-url:http://localhost:3000/login}")
    private String loginUrl;
    
    public void sendWelcomeEmail(String to, String name, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Hoşgeldiniz - Baskılı İşler Sistemi");
            message.setText(createWelcomeEmailContent(name, password));
            
            mailSender.send(message);
            log.info("Hoşgeldiniz emaili gönderildi: {}", to);
        } catch (Exception e) {
            log.error("Email gönderilirken hata oluştu: {}", e.getMessage());
            throw new RuntimeException("Email gönderilemedi", e);
        }
    }
    
    public void sendDealerAdminWelcomeEmail(String to, String adminName, String dealerName, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Dealer Admin Hesabınız Oluşturuldu - Baskılı İşler Sistemi");
            message.setText(createDealerAdminWelcomeEmailContent(adminName, dealerName, password));
            
            mailSender.send(message);
            log.info("Dealer admin hoşgeldiniz emaili gönderildi: {}", to);
        } catch (Exception e) {
            log.error("Dealer admin email gönderilirken hata oluştu: {}", e.getMessage());
            throw new RuntimeException("Email gönderilemedi", e);
        }
    }
    
    private String createWelcomeEmailContent(String name, String password) {
        return String.format("""
            Merhaba %s,
            
            Baskılı İşler sistemine hoşgeldiniz!
            
            Giriş bilgileriniz:
            Email: %s
            Şifre: %s
            
            Sisteme giriş yapmak için: %s
            
            Güvenliğiniz için lütfen ilk girişinizden sonra şifrenizi değiştirin.
            
            İyi çalışmalar,
            Baskılı İşler Ekibi
            """, name, name, password, loginUrl);
    }
    
    private String createDealerAdminWelcomeEmailContent(String adminName, String dealerName, String password) {
        return String.format("""
            Merhaba %s,
            
            %s bayisi için admin hesabınız başarıyla oluşturuldu!
            
            Giriş bilgileriniz:
            Email: %s
            Şifre: %s
            
            Sisteme giriş yapmak için: %s
            
            Bu hesap ile:
            - Bayi bilgilerinizi yönetebilirsiniz
            - Ürün kataloğunuzu oluşturabilirsiniz
            - Markalarınızı yönetebilirsiniz
            - Teklif ve siparişlerinizi takip edebilirsiniz
            
            Güvenliğiniz için lütfen ilk girişinizden sonra şifrenizi değiştirin.
            
            İyi çalışmalar,
            Baskılı İşler Ekibi
            """, adminName, dealerName, adminName, password, loginUrl);
    }
} 