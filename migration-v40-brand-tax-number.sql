-- Migration v40: Brand tablosuna vergi numarası kolonu ekleme
-- Tarih: 2024-12-19

-- Brand tablosuna tax_number kolonu ekleme (zorunlu değil)
ALTER TABLE brands ADD COLUMN tax_number VARCHAR(50);

-- Mevcut kayıtlar için NULL değer atama
UPDATE brands SET tax_number = NULL WHERE tax_number IS NULL;

-- Kolon için açıklama ekleme
COMMENT ON COLUMN brands.tax_number IS 'Marka vergi numarası (opsiyonel)'; 