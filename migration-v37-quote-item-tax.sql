-- Migration Script: v37-quote-item-tax
-- QuoteItem Entity'sine Tax Field'ları Ekleme
-- 1. tax_rate kolonu ekle (BigDecimal, default 18.00)
-- 2. tax_amount kolonu ekle (BigDecimal, computed)
-- 3. line_total_with_tax kolonu ekle (BigDecimal, computed)

-- =================================================================
-- QuoteItem Tax Fields Migration Script
-- =================================================================

-- 1. Yeni tax kolonlarını ekle
ALTER TABLE quote_item 
ADD COLUMN IF NOT EXISTS tax_rate NUMERIC(5,2) DEFAULT 18.00,
ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(10,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS line_total_with_tax NUMERIC(10,2) DEFAULT 0.00;

-- 2. Mevcut quote_item'lar için tax hesaplamalarını güncelle
UPDATE quote_item 
SET 
    tax_rate = 18.00,
    tax_amount = ROUND(line_total * 0.18, 2),
    line_total_with_tax = ROUND(line_total * 1.18, 2)
WHERE tax_rate IS NULL;

-- 3. Index'leri ekle (performans için)
CREATE INDEX IF NOT EXISTS idx_quote_item_tax_rate ON quote_item(tax_rate);

-- 4. Constraint'leri ekle
ALTER TABLE quote_item 
ADD CONSTRAINT IF NOT EXISTS chk_tax_rate_positive 
CHECK (tax_rate >= 0 AND tax_rate <= 100);

ALTER TABLE quote_item 
ADD CONSTRAINT IF NOT EXISTS chk_tax_amount_positive 
CHECK (tax_amount >= 0);

ALTER TABLE quote_item 
ADD CONSTRAINT IF NOT EXISTS chk_line_total_with_tax_positive 
CHECK (line_total_with_tax >= 0);

-- 5. Güncelleme tamamlandı
DO $$
BEGIN
    RAISE NOTICE 'QuoteItem tax fields migration completed successfully!';
END$$; 