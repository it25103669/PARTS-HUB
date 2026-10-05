-- ============================================================
-- PartsHub Pro  - 06_verify_database.sql
-- Database : PartsHub  |  Engine: MySQL 8.0
-- ============================================================
-- Verification script to confirm the database is correctly set up.
-- Run AFTER 05_seed_data.sql.
-- ============================================================

USE PartsHub;

-- ============================================================
-- SECTION 1: LIST ALL TABLES
-- ============================================================
SELECT '====== TABLES IN PartsHub DATABASE ======' AS section;
SHOW FULL TABLES;

-- ============================================================
-- SECTION 2: COLUMN / SCHEMA DETAILS PER TABLE
-- ============================================================
SELECT '====== TABLE SCHEMAS ======' AS section;

SELECT '--- user_accounts ---' AS tbl; DESCRIBE user_accounts;
SELECT '--- suppliers ---' AS tbl; DESCRIBE suppliers;
SELECT '--- supplier_parts ---' AS tbl; DESCRIBE supplier_parts;
SELECT '--- supplier_actions ---' AS tbl; DESCRIBE supplier_actions;
SELECT '--- inventory_items ---' AS tbl; DESCRIBE inventory_items;
SELECT '--- purchase_orders ---' AS tbl; DESCRIBE purchase_orders;
SELECT '--- purchase_order_items ---' AS tbl; DESCRIBE purchase_order_items;
SELECT '--- sale_invoices ---' AS tbl; DESCRIBE sale_invoices;
SELECT '--- sale_invoice_items ---' AS tbl; DESCRIBE sale_invoice_items;
SELECT '--- return_claims ---' AS tbl; DESCRIBE return_claims;
SELECT '--- promotional_campaign ---' AS tbl; DESCRIBE promotional_campaign;

-- ============================================================
-- SECTION 3: PRIMARY KEY VERIFICATION
-- ============================================================
SELECT '====== PRIMARY KEYS ======' AS section;
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_KEY,
    EXTRA
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'PartsHub'
  AND COLUMN_KEY = 'PRI'
ORDER BY TABLE_NAME;

-- ============================================================
-- SECTION 4: FOREIGN KEY VERIFICATION
-- ============================================================
SELECT '====== FOREIGN KEY CONSTRAINTS ======' AS section;
SELECT
    kcu.TABLE_NAME,
    kcu.COLUMN_NAME,
    kcu.CONSTRAINT_NAME,
    kcu.REFERENCED_TABLE_NAME,
    kcu.REFERENCED_COLUMN_NAME,
    rc.DELETE_RULE,
    rc.UPDATE_RULE
FROM information_schema.KEY_COLUMN_USAGE kcu
JOIN information_schema.REFERENTIAL_CONSTRAINTS rc
     ON rc.CONSTRAINT_NAME   = kcu.CONSTRAINT_NAME
    AND rc.CONSTRAINT_SCHEMA = kcu.TABLE_SCHEMA
WHERE kcu.TABLE_SCHEMA = 'PartsHub'
  AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY kcu.TABLE_NAME;

-- ============================================================
-- SECTION 5: UNIQUE CONSTRAINT VERIFICATION
-- ============================================================
SELECT '====== UNIQUE CONSTRAINTS / INDEXES ======' AS section;
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'PartsHub'
  AND NON_UNIQUE = 0
  AND INDEX_NAME != 'PRIMARY'
ORDER BY TABLE_NAME, INDEX_NAME;

-- ============================================================
-- SECTION 6: CHECK CONSTRAINTS
-- ============================================================
SELECT '====== CHECK CONSTRAINTS ======' AS section;
SELECT
    tc.TABLE_NAME,
    cc.CONSTRAINT_NAME,
    cc.CHECK_CLAUSE
FROM information_schema.CHECK_CONSTRAINTS cc
JOIN information_schema.TABLE_CONSTRAINTS tc
  ON cc.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
 AND cc.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
WHERE cc.CONSTRAINT_SCHEMA = 'PartsHub'
ORDER BY tc.TABLE_NAME;

-- ============================================================
-- SECTION 7: ROW COUNTS (all tables)
-- ============================================================
SELECT '====== ROW COUNTS ======' AS section;
SELECT 'user_accounts'       AS tbl, COUNT(*) AS row_count FROM user_accounts
UNION ALL
SELECT 'suppliers',                  COUNT(*) FROM suppliers
UNION ALL
SELECT 'supplier_parts',             COUNT(*) FROM supplier_parts
UNION ALL
SELECT 'supplier_actions',           COUNT(*) FROM supplier_actions
UNION ALL
SELECT 'inventory_items',            COUNT(*) FROM inventory_items
UNION ALL
SELECT 'purchase_orders',            COUNT(*) FROM purchase_orders
UNION ALL
SELECT 'purchase_order_items',       COUNT(*) FROM purchase_order_items
UNION ALL
SELECT 'sale_invoices',              COUNT(*) FROM sale_invoices
UNION ALL
SELECT 'sale_invoice_items',         COUNT(*) FROM sale_invoice_items
UNION ALL
SELECT 'return_claims',              COUNT(*) FROM return_claims
UNION ALL
SELECT 'promotional_campaign',       COUNT(*) FROM promotional_campaign;

-- ============================================================
-- SECTION 8: BUSINESS DATA VALIDATION QUERIES
-- ============================================================
SELECT '====== BUSINESS DATA VALIDATION ======' AS section;

-- Suppliers by tier
SELECT '--- Supplier Tier Distribution ---' AS q;
SELECT tier, COUNT(*) AS count, AVG(score) AS avg_score
FROM suppliers GROUP BY tier ORDER BY avg_score DESC;

-- Inventory stock status
SELECT '--- Inventory Status Summary ---' AS q;
SELECT status, COUNT(*) AS count, SUM(qty_on_hand * selling_price) AS total_value
FROM inventory_items GROUP BY status;

-- Low/out of stock items
SELECT '--- Items At or Below Reorder Level ---' AS q;
SELECT part_name, sku_code, qty_on_hand, reorder_level, status
FROM inventory_items
WHERE qty_on_hand <= reorder_level
ORDER BY qty_on_hand;

-- Purchase orders by status
SELECT '--- Purchase Order Status Summary ---' AS q;
SELECT status, COUNT(*) AS count, SUM(total_amount) AS total_value
FROM purchase_orders GROUP BY status;

-- Sales revenue summary
SELECT '--- Sales Revenue Summary ---' AS q;
SELECT payment_status, COUNT(*) AS invoices, SUM(grand_total) AS total_revenue
FROM sale_invoices GROUP BY payment_status;

-- Return claims by type and status
SELECT '--- Return Claims Summary ---' AS q;
SELECT claim_type, status, COUNT(*) AS count
FROM return_claims GROUP BY claim_type, status ORDER BY claim_type, status;

-- Active campaigns
SELECT '--- Promotional Campaigns ---' AS q;
SELECT campaign_name, status, discount_percentage, start_date, end_date
FROM promotional_campaign ORDER BY start_date;

-- User accounts (no passwords)
SELECT '--- User Accounts (no passwords) ---' AS q;
SELECT username, full_name, role, student_id, title FROM user_accounts;

-- ============================================================
-- SECTION 9: INTEGRATION / REFERENTIAL INTEGRITY CHECKS
-- ============================================================
SELECT '====== REFERENTIAL INTEGRITY CHECKS ======' AS section;

-- PO items whose PO exists
SELECT '--- Orphaned PO Items (should be 0) ---' AS q;
SELECT COUNT(*) AS orphaned_po_items
FROM purchase_order_items poi
LEFT JOIN purchase_orders po ON po.id = poi.purchase_order_id
WHERE po.id IS NULL;

-- Sale items whose invoice exists
SELECT '--- Orphaned Sale Items (should be 0) ---' AS q;
SELECT COUNT(*) AS orphaned_sale_items
FROM sale_invoice_items sii
LEFT JOIN sale_invoices si ON si.id = sii.sale_invoice_id
WHERE si.id IS NULL;

-- Supplier parts with valid supplier
SELECT '--- Supplier Parts with Missing Supplier (should be 0) ---' AS q;
SELECT COUNT(*) AS dangling_supplier_parts
FROM supplier_parts sp
LEFT JOIN suppliers s ON s.id = sp.supplier_id
WHERE s.id IS NULL;

SELECT '====== VERIFICATION COMPLETE ======' AS section;
