-- ============================================================
-- PartsHub Pro  - 04_create_indexes.sql
-- Database : PartsHub  |  Engine: MySQL 8.0
-- ============================================================
-- Performance indexes for common query patterns.
-- Run AFTER 03_create_constraints.sql.
-- ============================================================

USE PartsHub;

-- Helper procedure to add index idempotently
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

DELIMITER //
CREATE PROCEDURE add_index_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_column VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.statistics 
        WHERE table_schema = DATABASE() 
          AND table_name = p_table 
          AND index_name = p_index
    ) THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index, ' ON ', p_table, ' (', p_column, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ============================================================
-- user_accounts indexes
-- ============================================================
CALL add_index_if_not_exists('user_accounts', 'idx_user_accounts_role', 'role');

-- ============================================================
-- suppliers indexes
-- ============================================================
CALL add_index_if_not_exists('suppliers', 'idx_suppliers_supplier_name', 'supplier_name');
CALL add_index_if_not_exists('suppliers', 'idx_suppliers_status', 'status');
CALL add_index_if_not_exists('suppliers', 'idx_suppliers_category', 'category');
CALL add_index_if_not_exists('suppliers', 'idx_suppliers_tier', 'tier');

-- ============================================================
-- supplier_parts indexes
-- ============================================================
CALL add_index_if_not_exists('supplier_parts', 'idx_supplier_parts_supplier_id', 'supplier_id');
CALL add_index_if_not_exists('supplier_parts', 'idx_supplier_parts_sku_code', 'sku_code');
CALL add_index_if_not_exists('supplier_parts', 'idx_supplier_parts_category', 'category');
CALL add_index_if_not_exists('supplier_parts', 'idx_supplier_parts_availability', 'availability_status');

-- ============================================================
-- supplier_actions indexes
-- ============================================================
CALL add_index_if_not_exists('supplier_actions', 'idx_supplier_actions_supplier_id', 'supplier_id');
CALL add_index_if_not_exists('supplier_actions', 'idx_supplier_actions_action_date', 'action_date');

-- ============================================================
-- inventory_items indexes
-- ============================================================
CALL add_index_if_not_exists('inventory_items', 'idx_inventory_part_name', 'part_name');
CALL add_index_if_not_exists('inventory_items', 'idx_inventory_category', 'category');
CALL add_index_if_not_exists('inventory_items', 'idx_inventory_status', 'status');
CALL add_index_if_not_exists('inventory_items', 'idx_inventory_supplier_name', 'supplier_name');

-- ============================================================
-- purchase_orders indexes
-- ============================================================
CALL add_index_if_not_exists('purchase_orders', 'idx_po_supplier_name', 'supplier_name');
CALL add_index_if_not_exists('purchase_orders', 'idx_po_order_date', 'order_date');
CALL add_index_if_not_exists('purchase_orders', 'idx_po_status', 'status');

-- ============================================================
-- purchase_order_items indexes
-- ============================================================
CALL add_index_if_not_exists('purchase_order_items', 'idx_po_items_purchase_order_id', 'purchase_order_id');

-- ============================================================
-- sale_invoices indexes
-- ============================================================
CALL add_index_if_not_exists('sale_invoices', 'idx_sale_invoices_customer_name', 'customer_name');
CALL add_index_if_not_exists('sale_invoices', 'idx_sale_invoices_sale_date', 'sale_date');
CALL add_index_if_not_exists('sale_invoices', 'idx_sale_invoices_payment_status', 'payment_status');

-- ============================================================
-- sale_invoice_items indexes
-- ============================================================
CALL add_index_if_not_exists('sale_invoice_items', 'idx_sale_items_sale_invoice_id', 'sale_invoice_id');

-- ============================================================
-- return_claims indexes
-- ============================================================
CALL add_index_if_not_exists('return_claims', 'idx_return_claims_status', 'status');
CALL add_index_if_not_exists('return_claims', 'idx_return_claims_claim_type', 'claim_type');
CALL add_index_if_not_exists('return_claims', 'idx_return_claims_reference_number', 'reference_number');

-- ============================================================
-- promotional_campaign indexes
-- ============================================================
CALL add_index_if_not_exists('promotional_campaign', 'idx_campaign_status', 'status');
CALL add_index_if_not_exists('promotional_campaign', 'idx_campaign_start_date', 'start_date');
CALL add_index_if_not_exists('promotional_campaign', 'idx_campaign_end_date', 'end_date');

-- Cleanup helper procedure
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

SELECT 'All indexes created successfully.' AS status;

