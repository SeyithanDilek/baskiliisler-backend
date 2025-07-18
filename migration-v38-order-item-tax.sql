-- Migration Script: v38-order-item-tax
-- OrderItem Entity'sine Tax Field'ları Ekleme
-- 1. tax_rate kolonu ekle (BigDecimal, default 18.00)
-- 2. tax_amount kolonu ekle (BigDecimal, computed)
-- 3. line_total_with_tax kolonu ekle (BigDecimal, computed)

-- =================================================================
-- OrderItem Tax Fields Migration Script
-- =================================================================

-- 1. Yeni tax kolonlarını ekle
ALTER TABLE order_item 
ADD COLUMN IF NOT EXISTS tax_rate NUMERIC(5,2) DEFAULT 18.00,
ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(10,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS line_total_with_tax NUMERIC(10,2) DEFAULT 0.00;

-- 2. Mevcut order_item'lar için tax hesaplamalarını güncelle
UPDATE order_item 
SET 
    tax_rate = 18.00,
    tax_amount = ROUND(line_total * 0.18, 2),
    line_total_with_tax = ROUND(line_total * 1.18, 2)
WHERE tax_rate IS NULL;

-- 3. Index'leri ekle (performans için)
CREATE INDEX IF NOT EXISTS idx_order_item_tax_rate ON order_item(tax_rate);

-- 4. Constraint'leri ekle
ALTER TABLE order_item 
ADD CONSTRAINT IF NOT EXISTS chk_order_item_tax_rate_positive 
CHECK (tax_rate >= 0 AND tax_rate <= 100);

ALTER TABLE order_item 
ADD CONSTRAINT IF NOT EXISTS chk_order_item_tax_amount_positive 
CHECK (tax_amount >= 0);

ALTER TABLE order_item 
ADD CONSTRAINT IF NOT EXISTS chk_order_item_line_total_with_tax_positive 
CHECK (line_total_with_tax >= 0);

-- 5. Order tablosunun total_price'ını güncelle (KDV dahil)
UPDATE orders 
SET total_price = (
    SELECT COALESCE(SUM(line_total_with_tax), 0)
    FROM order_item 
    WHERE order_id = orders.id
)
WHERE EXISTS (
    SELECT 1 FROM order_item WHERE order_id = orders.id
);

-- 6. Güncelleme tamamlandı
DO $$
BEGIN
    RAISE NOTICE 'OrderItem tax fields migration completed successfully!';
END$$; 