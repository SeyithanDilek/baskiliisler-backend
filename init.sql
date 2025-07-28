-- Baskılı İşler Database Initialization Script

-- Database zaten docker-compose.yml'de oluşturuluyor
-- Burada sadece gerekli extension'ları ve initial data'yı ekliyoruz

-- UUID extension'ını aktifleştir (gerekirse)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- TimeZone ayarla
SET timezone = 'Europe/Istanbul';

-- Database encoding kontrolü
SHOW server_encoding;
SHOW client_encoding;

-- Initial yönetici kullanıcısı (password: admin123 - hash'lenmiş hali)
-- Bu Spring Boot start'ta otomatik olarak oluşturulacak
-- Burada sadece placeholder olarak bırakıyoruz

-- Log mesajı
SELECT 'Baskılı İşler Database başarıyla initialize edildi!' AS message; 