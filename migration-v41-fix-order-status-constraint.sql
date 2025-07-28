-- Migration v41: Order status constraint düzeltme
-- Tarih: 2025-07-28
-- Sorun: orders_status_check constraint'i IN_WAREHOUSE değerini kabul etmiyor
-- Çözüm: Constraint'i güncelleyerek tüm OrderStatus enum değerlerini kabul etmesini sağlama

-- Mevcut constraint'i kaldır
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

-- Yeni constraint ekle - tüm OrderStatus enum değerlerini kabul et
ALTER TABLE orders ADD CONSTRAINT orders_status_check 
    CHECK (status IN ('PENDING', 'IN_PRODUCTION', 'IN_WAREHOUSE', 'IN_TRANSIT', 'DELIVERED'));

-- Constraint için açıklama ekle
COMMENT ON CONSTRAINT orders_status_check ON orders IS 'Order status enum değerlerini kontrol eder'; 