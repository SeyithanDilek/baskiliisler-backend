-- Migration v39: User tablosuna phone_number kolonu ekleme
-- Tarih: 2024-12-19

-- User tablosuna phone_number kolonu ekle
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20) NOT NULL DEFAULT '+90 555 000 00 00';

-- Mevcut kullanıcılar için varsayılan telefon numarası ata
UPDATE users SET phone_number = '+90 555 000 00 00' WHERE phone_number IS NULL;

-- Role enum değerlerini güncelle
-- Önce eski enum değerlerini yeni değerlerle değiştir
UPDATE users SET role = 'DEALER_USER' WHERE role = 'REP';
UPDATE users SET role = 'FACTORY_USER' WHERE role = 'FAB';
UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'ADMIN';

-- Role kolonunun uzunluğunu artır (yeni enum değerleri daha uzun)
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20); 