-- Migration Script: v32-product-updates
-- Product Entity Değişiklikleri
-- 1. code field'ı kaldır
-- 2. description field'ı ekle (TEXT)
-- 3. taxRate field'ı ekle (BigDecimal, default 18.00)
-- 4. unit field'ını String'den enum'a çevir

-- =================================================================
-- Product Entity Migration Script
-- =================================================================

-- 1. Unit enum'unu oluştur (eğer yoksa)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'unit_enum') THEN
        CREATE TYPE unit_enum AS ENUM ('ADET', 'KG', 'METRE', 'LITRE', 'M2', 'CM', 'MM', 'TON', 'GRAM');
    END IF;
END$$;

-- 2. Yeni kolonları ekle
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS tax_rate NUMERIC(5,2) DEFAULT 18.00;

-- 3. Mevcut unit değerlerini enum'a uyumlu hale getir
UPDATE products 
SET unit = CASE 
    WHEN LOWER(unit) IN ('adet', 'piece', 'pcs') THEN 'ADET'
    WHEN LOWER(unit) IN ('kg', 'kilogram') THEN 'KG'
    WHEN LOWER(unit) IN ('m', 'metre', 'meter') THEN 'METRE'
    WHEN LOWER(unit) IN ('lt', 'litre', 'liter') THEN 'LITRE'
    WHEN LOWER(unit) IN ('m2', 'metrekare', 'sqm') THEN 'M2'
    WHEN LOWER(unit) IN ('cm', 'santimetre') THEN 'CM'
    WHEN LOWER(unit) IN ('mm', 'milimetre') THEN 'MM'
    WHEN LOWER(unit) IN ('ton', 'tons') THEN 'TON'
    WHEN LOWER(unit) IN ('gr', 'gram', 'grams') THEN 'GRAM'
    ELSE 'ADET'
END;

-- 4. unit kolonunu enum tipine çevir
ALTER TABLE products 
ALTER COLUMN unit TYPE unit_enum USING unit::unit_enum;

-- 5. code kolonunu kaldır (eğer varsa)
ALTER TABLE products 
DROP COLUMN IF EXISTS code;

-- 6. Yeni kolonlar için NOT NULL constraint ekle
ALTER TABLE products 
ALTER COLUMN tax_rate SET NOT NULL;

-- 7. Mevcut kayıtlar için default description ekle
UPDATE products 
SET description = COALESCE(description, 'Ürün açıklaması') 
WHERE description IS NULL;

-- 8. Index'leri güncelle (performans için)
-- Eski code index'ini kaldır (eğer varsa)
DROP INDEX IF EXISTS idx_products_code;

-- Yeni description için index ekle (arama performansı için)
CREATE INDEX IF NOT EXISTS idx_products_description ON products USING gin(to_tsvector('turkish', description));

-- 9. Constraint'leri kontrol et ve güncelle
-- unit_price NOT NULL olmalı
ALTER TABLE products 
ALTER COLUMN unit_price SET NOT NULL;

-- name NOT NULL olmalı
ALTER TABLE products 
ALTER COLUMN name SET NOT NULL;

-- active default false olmalı
ALTER TABLE products 
ALTER COLUMN active SET DEFAULT true;

-- =================================================================
-- Verification Queries (Doğrulama için)
-- =================================================================

-- Tablo yapısını kontrol et
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'products' 
ORDER BY ordinal_position;

-- Enum değerlerini kontrol et
SELECT enumlabel FROM pg_enum WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'unit_enum');

-- Örnek veri kontrolü
SELECT 
    id, 
    name, 
    description, 
    unit, 
    unit_price, 
    tax_rate, 
    active 
FROM products 
LIMIT 5;

-- =================================================================
-- Rollback Script (Geri alma için)
-- =================================================================

/*
-- Eğer geri almak isterseniz:

-- 1. code kolonunu geri ekle
ALTER TABLE products ADD COLUMN code VARCHAR(50);

-- 2. Otomatik code değerleri üret
UPDATE products SET code = 'PROD_' || LPAD(id::text, 3, '0');

-- 3. code'u unique yap
ALTER TABLE products ADD CONSTRAINT uk_products_code UNIQUE (code);

-- 4. unit'i string'e çevir
ALTER TABLE products ALTER COLUMN unit TYPE VARCHAR(50);

-- 5. Yeni kolonları kaldır
ALTER TABLE products DROP COLUMN description;
ALTER TABLE products DROP COLUMN tax_rate;

-- 6. Enum'u kaldır
DROP TYPE IF EXISTS unit_enum;
*/ 