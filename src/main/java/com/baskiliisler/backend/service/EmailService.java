package com.baskiliisler.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.login-url:http://localhost:3000/login}")
    private String loginUrl;
    
    @Value("${app.reset-password-url:http://localhost:3000/reset-password}")
    private String resetPasswordUrl;
    
    public void sendWelcomeEmail(String to, String name, String password) {
        try {
            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                messageHelper.setTo(to);
                messageHelper.setSubject("Hoşgeldiniz - Baskılı İşler Sistemi");
                messageHelper.setText(createWelcomeHtmlEmailContent(to, name, password), true);
            };
            
            mailSender.send(messagePreparator);
            log.info("Hoşgeldiniz HTML emaili gönderildi: {}", to);
        } catch (Exception e) {
            log.error("Email gönderilirken hata oluştu: {}", e.getMessage());
            throw new RuntimeException("Email gönderilemedi", e);
        }
    }
    
    public void sendDealerAdminWelcomeEmail(String to, String adminName, String dealerName, String password) {
        try {
            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                messageHelper.setTo(to);
                messageHelper.setSubject("Dealer Admin Hesabınız Oluşturuldu - Baskılı İşler Sistemi");
                messageHelper.setText(createDealerAdminWelcomeHtmlEmailContent(to, adminName, dealerName, password), true);
            };
            
            mailSender.send(messagePreparator);
            log.info("Dealer admin hoşgeldiniz HTML emaili gönderildi: {}", to);
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

    private String createWelcomeHtmlEmailContent(String email, String name, String password) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="tr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Hoşgeldiniz - Baskılı İşler Sistemi</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; }
                    .logo { width: 80px; height: 80px; margin: 0 auto 20px; display: block; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 300; }
                    .content { padding: 40px 30px; }
                    .greeting { font-size: 18px; color: #333; margin-bottom: 25px; }
                    .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
                    .credentials { background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 25px; margin: 25px 0; }
                    .credentials h3 { color: #495057; margin: 0 0 15px 0; font-size: 16px; }
                    .credential-item { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #dee2e6; }
                    .credential-item:last-child { border-bottom: none; }
                    .credential-label { font-weight: 600; color: #495057; }
                    .credential-value { color: #6c757d; font-family: 'Courier New', monospace; }
                    .button-container { text-align: center; margin: 35px 0; }
                    .login-button { display: inline-block; background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: #ffffff; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(40, 167, 69, 0.4); }
                    .login-button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(40, 167, 69, 0.6); }
                    .security-note { background-color: #d1ecf1; border: 1px solid #bee5eb; border-radius: 8px; padding: 20px; margin: 25px 0; }
                    .security-note h3 { color: #0c5460; margin: 0 0 10px 0; font-size: 16px; }
                    .security-note p { color: #0c5460; margin: 0; font-size: 14px; }
                    .footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }
                    .footer p { color: #6c757d; margin: 0; font-size: 14px; }
                    .company-info { margin-top: 15px; }
                    .company-info strong { color: #495057; }
                    @media (max-width: 600px) { .container { margin: 10px; } .header { padding: 20px; } .content { padding: 25px 20px; } }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">
                            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="50" cy="50" r="45" fill="none" stroke="#ffffff" stroke-width="2"/>
                                <path d="M25 30 Q35 20 45 30 Q55 20 65 30 Q75 20 75 30 Q85 40 75 50 Q85 60 75 70 Q65 80 55 70 Q45 80 35 70 Q25 60 25 50 Q25 40 25 30" fill="#ffffff" opacity="0.9"/>
                                <text x="50" y="45" text-anchor="middle" fill="#ffffff" font-size="24" font-weight="bold">B</text>
                                <text x="50" y="65" text-anchor="middle" fill="#ffffff" font-size="16" font-weight="bold">i</text>
                            </svg>
                        </div>
                        <h1>Baskılı İşler Sistemi</h1>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">Merhaba %s,</div>
                        
                        <div class="message">
                            Baskılı İşler sistemine hoşgeldiniz! Hesabınız başarıyla oluşturuldu ve artık sistemi kullanmaya başlayabilirsiniz.
                        </div>
                        
                        <div class="credentials">
                            <h3>🔐 Giriş Bilgileriniz</h3>
                            <div class="credential-item">
                                <span class="credential-label">Email:</span>
                                <span class="credential-value">%s</span>
                            </div>
                            <div class="credential-item">
                                <span class="credential-label">Şifre:</span>
                                <span class="credential-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="button-container">
                            <a href="%s" class="login-button">Sisteme Giriş Yap</a>
                        </div>
                        
                        <div class="security-note">
                            <h3>🔒 Güvenlik Notu</h3>
                            <p>Güvenliğiniz için lütfen <strong>ilk girişinizden sonra şifrenizi değiştirin</strong>.</p>
                        </div>
                        
                        <div class="message">
                            Herhangi bir sorunuz olursa, destek ekibimiz size yardımcı olmaktan memnuniyet duyacaktır.
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>Bu email Baskılı İşler Sistemi tarafından otomatik olarak gönderilmiştir.</p>
                        <div class="company-info">
                            <strong>Baskılı İşler</strong><br>
                            Profesyonel baskı ve tasarım çözümleri
                        </div>
                    </div>
                </div>
            </body>
            </html>
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

    private String createDealerAdminWelcomeHtmlEmailContent(String email, String adminName, String dealerName, String password) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="tr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Dealer Admin Hesabı Oluşturuldu - Baskılı İşler Sistemi</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; }
                    .logo { width: 80px; height: 80px; margin: 0 auto 20px; display: block; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 300; }
                    .content { padding: 40px 30px; }
                    .greeting { font-size: 18px; color: #333; margin-bottom: 25px; }
                    .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
                    .dealer-info { background-color: #e3f2fd; border: 1px solid #bbdefb; border-radius: 8px; padding: 25px; margin: 25px 0; }
                    .dealer-info h3 { color: #1565c0; margin: 0 0 15px 0; font-size: 16px; }
                    .dealer-name { font-size: 20px; font-weight: 600; color: #1565c0; text-align: center; margin-bottom: 15px; }
                    .credentials { background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 25px; margin: 25px 0; }
                    .credentials h3 { color: #495057; margin: 0 0 15px 0; font-size: 16px; }
                    .credential-item { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #dee2e6; }
                    .credential-item:last-child { border-bottom: none; }
                    .credential-label { font-weight: 600; color: #495057; }
                    .credential-value { color: #6c757d; font-family: 'Courier New', monospace; }
                    .features { background-color: #f1f8e9; border: 1px solid #c5e1a5; border-radius: 8px; padding: 25px; margin: 25px 0; }
                    .features h3 { color: #33691e; margin: 0 0 15px 0; font-size: 16px; }
                    .feature-list { list-style: none; padding: 0; margin: 0; }
                    .feature-list li { padding: 8px 0; color: #33691e; position: relative; padding-left: 25px; }
                    .feature-list li:before { content: "✓"; position: absolute; left: 0; color: #4caf50; font-weight: bold; }
                    .button-container { text-align: center; margin: 35px 0; }
                    .login-button { display: inline-block; background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: #ffffff; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(40, 167, 69, 0.4); }
                    .login-button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(40, 167, 69, 0.6); }
                    .security-note { background-color: #d1ecf1; border: 1px solid #bee5eb; border-radius: 8px; padding: 20px; margin: 25px 0; }
                    .security-note h3 { color: #0c5460; margin: 0 0 10px 0; font-size: 16px; }
                    .security-note p { color: #0c5460; margin: 0; font-size: 14px; }
                    .footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }
                    .footer p { color: #6c757d; margin: 0; font-size: 14px; }
                    .company-info { margin-top: 15px; }
                    .company-info strong { color: #495057; }
                    @media (max-width: 600px) { .container { margin: 10px; } .header { padding: 20px; } .content { padding: 25px 20px; } }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">
                            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="50" cy="50" r="45" fill="none" stroke="#ffffff" stroke-width="2"/>
                                <path d="M25 30 Q35 20 45 30 Q55 20 65 30 Q75 20 75 30 Q85 40 75 50 Q85 60 75 70 Q65 80 55 70 Q45 80 35 70 Q25 60 25 50 Q25 40 25 30" fill="#ffffff" opacity="0.9"/>
                                <text x="50" y="45" text-anchor="middle" fill="#ffffff" font-size="24" font-weight="bold">B</text>
                                <text x="50" y="65" text-anchor="middle" fill="#ffffff" font-size="16" font-weight="bold">i</text>
                            </svg>
                        </div>
                        <h1>Baskılı İşler Sistemi</h1>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">Merhaba %s,</div>
                        
                        <div class="message">
                            Tebrikler! <strong>%s</strong> bayisi için admin hesabınız başarıyla oluşturuldu. Artık bayinizi yönetmeye başlayabilirsiniz.
                        </div>
                        
                        <div class="dealer-info">
                            <h3>🏢 Bayi Bilgileri</h3>
                            <div class="dealer-name">%s</div>
                        </div>
                        
                        <div class="credentials">
                            <h3>🔐 Giriş Bilgileriniz</h3>
                            <div class="credential-item">
                                <span class="credential-label">Email:</span>
                                <span class="credential-value">%s</span>
                            </div>
                            <div class="credential-item">
                                <span class="credential-label">Şifre:</span>
                                <span class="credential-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="features">
                            <h3>🚀 Hesabınızla Yapabilecekleriniz</h3>
                            <ul class="feature-list">
                                <li>Bayi bilgilerinizi yönetebilirsiniz</li>
                                <li>Ürün kataloğunuzu oluşturabilirsiniz</li>
                                <li>Markalarınızı yönetebilirsiniz</li>
                                <li>Teklif ve siparişlerinizi takip edebilirsiniz</li>
                                <li>Müşteri ilişkilerinizi geliştirebilirsiniz</li>
                            </ul>
                        </div>
                        
                        <div class="button-container">
                            <a href="%s" class="login-button">Sisteme Giriş Yap</a>
                        </div>
                        
                        <div class="security-note">
                            <h3>🔒 Güvenlik Notu</h3>
                            <p>Güvenliğiniz için lütfen <strong>ilk girişinizden sonra şifrenizi değiştirin</strong>.</p>
                        </div>
                        
                        <div class="message">
                            Herhangi bir sorunuz olursa, destek ekibimiz size yardımcı olmaktan memnuniyet duyacaktır.
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>Bu email Baskılı İşler Sistemi tarafından otomatik olarak gönderilmiştir.</p>
                        <div class="company-info">
                            <strong>Baskılı İşler</strong><br>
                            Profesyonel baskı ve tasarım çözümleri
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, email, adminName, dealerName, dealerName, email, password, loginUrl);
    }
    
    public void sendPasswordResetEmail(String to, String name, String token) {
        try {
            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                messageHelper.setTo(to);
                messageHelper.setSubject("Şifre Sıfırlama - Baskılı İşler Sistemi");
                messageHelper.setText(createPasswordResetHtmlEmailContent(name, token), true);
            };
            
            mailSender.send(messagePreparator);
            log.info("Şifre sıfırlama HTML emaili gönderildi: {}", to);
        } catch (Exception e) {
            log.error("Şifre sıfırlama emaili gönderilirken hata oluştu: {}", e.getMessage());
            throw new RuntimeException("Şifre sıfırlama emaili gönderilemedi", e);
        }
    }
    
    private String createPasswordResetEmailContent(String name, String token) {
        return String.format("""
            Merhaba %s,
            
            Şifrenizi sıfırlamak için aşağıdaki linke tıklayın:
            %s?token=%s
            
            Bu link 1 saat sonra geçersiz olacaktır.
            
            Eğer bu isteği siz yapmadıysanız, bu emaili görmezden gelebilirsiniz.
            
            Güvenliğiniz için lütfen şifrenizi kimseyle paylaşmayın.
            
            İyi çalışmalar,
            Baskılı İşler Ekibi
            """, name, resetPasswordUrl, token);
    }

    private String createPasswordResetHtmlEmailContent(String name, String token) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="tr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Şifre Sıfırlama</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; }
                    .logo { width: 80px; height: 80px; margin: 0 auto 20px; display: block; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 300; }
                    .content { padding: 40px 30px; }
                    .greeting { font-size: 18px; color: #333; margin-bottom: 25px; }
                    .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
                    .button-container { text-align: center; margin: 35px 0; }
                    .reset-button { display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); }
                    .reset-button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6); }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin: 25px 0; }
                    .warning h3 { color: #856404; margin: 0 0 10px 0; font-size: 16px; }
                    .warning p { color: #856404; margin: 0; font-size: 14px; }
                    .footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }
                    .footer p { color: #6c757d; margin: 0; font-size: 14px; }
                    .company-info { margin-top: 15px; }
                    .company-info strong { color: #495057; }
                    @media (max-width: 600px) { .container { margin: 10px; } .header { padding: 20px; } .content { padding: 25px 20px; } }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">
                            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="50" cy="50" r="45" fill="none" stroke="#ffffff" stroke-width="2"/>
                                <path d="M25 30 Q35 20 45 30 Q55 20 65 30 Q75 20 75 30 Q85 40 75 50 Q85 60 75 70 Q65 80 55 70 Q45 80 35 70 Q25 60 25 50 Q25 40 25 30" fill="#ffffff" opacity="0.9"/>
                                <text x="50" y="45" text-anchor="middle" fill="#ffffff" font-size="24" font-weight="bold">B</text>
                                <text x="50" y="65" text-anchor="middle" fill="#ffffff" font-size="16" font-weight="bold">i</text>
                            </svg>
                        </div>
                        <h1>Baskılı İşler Sistemi</h1>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">Merhaba %s,</div>
                        
                        <div class="message">
                            Şifrenizi sıfırlamak için aşağıdaki butona tıklayın. Bu işlem güvenli ve hızlıdır.
                        </div>
                        
                        <div class="button-container">
                            <a href="%s?token=%s" class="reset-button">Şifremi Sıfırla</a>
                        </div>
                        
                        <div class="warning">
                            <h3>⚠️ Önemli Bilgi</h3>
                            <p>Bu link <strong>1 saat sonra geçersiz</strong> olacaktır. Güvenliğiniz için lütfen şifrenizi kimseyle paylaşmayın.</p>
                        </div>
                        
                        <div class="message">
                            Eğer bu isteği siz yapmadıysanız, bu emaili görmezden gelebilirsiniz. Hesabınız güvende kalacaktır.
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>Bu email Baskılı İşler Sistemi tarafından otomatik olarak gönderilmiştir.</p>
                        <div class="company-info">
                            <strong>Baskılı İşler</strong><br>
                            Profesyonel baskı ve tasarım çözümleri
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, name, resetPasswordUrl, token);
    }
} 