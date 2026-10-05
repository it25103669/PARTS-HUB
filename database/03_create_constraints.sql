-- ============================================================
-- PartsHub Pro  - 03_create_constraints.sql
-- Database : PartsHub  |  Engine: MySQL 8.0
-- ============================================================
-- Adds constraints that were not defined inline in 02_create_tables.sql.
-- Run AFTER 02_create_tables.sql.
-- Uses IF NOT EXISTS / checks for constraint existence before adding
-- so this script can be re-run safely.
-- ============================================================

USE PartsHub;

-- ============================================================
-- UNIQUE CONSTRAINTS
-- ============================================================

-- user_accounts.username must be unique
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'user_accounts'
      AND CONSTRAINT_NAME = 'uk_user_accounts_username'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE user_accounts ADD CONSTRAINT uk_user_accounts_username UNIQUE (username)',
    'SELECT ''uk_user_accounts_username already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- suppliers.supplier_code must be unique
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'suppliers'
      AND CONSTRAINT_NAME = 'uk_suppliers_supplier_code'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE suppliers ADD CONSTRAINT uk_suppliers_supplier_code UNIQUE (supplier_code)',
    'SELECT ''uk_suppliers_supplier_code already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- inventory_items.sku_code must be unique
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'inventory_items'
      AND CONSTRAINT_NAME = 'uk_inventory_items_sku_code'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE inventory_items ADD CONSTRAINT uk_inventory_items_sku_code UNIQUE (sku_code)',
    'SELECT ''uk_inventory_items_sku_code already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- purchase_orders.po_number must be unique
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'purchase_orders'
      AND CONSTRAINT_NAME = 'uk_purchase_orders_po_number'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE purchase_orders ADD CONSTRAINT uk_purchase_orders_po_number UNIQUE (po_number)',
    'SELECT ''uk_purchase_orders_po_number already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sale_invoices.invoice_number must be unique
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'sale_invoices'
      AND CONSTRAINT_NAME = 'uk_sale_invoices_invoice_number'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE sale_invoices ADD CONSTRAINT uk_sale_invoices_invoice_number UNIQUE (invoice_number)',
    'SELECT ''uk_sale_invoices_invoice_number already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- return_claims.claim_number must be unique
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'return_claims'
      AND CONSTRAINT_NAME = 'uk_return_claims_claim_number'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE return_claims ADD CONSTRAINT uk_return_claims_claim_number UNIQUE (claim_number)',
    'SELECT ''uk_return_claims_claim_number already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- FOREIGN KEY CONSTRAINTS
-- ============================================================

-- supplier_parts.supplier_id -> suppliers(id)
-- NOTE: ON DELETE CASCADE matches the original schema.sql intent.
--       The Java entity stores supplier_id as a plain Long without
--       @ManyToOne, so cascade behaviour here is a DB-level safety net.
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'supplier_parts'
      AND CONSTRAINT_NAME = 'fk_supplier_parts_supplier'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE supplier_parts ADD CONSTRAINT fk_supplier_parts_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT ''fk_supplier_parts_supplier already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- supplier_actions.supplier_id -> suppliers(id)
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'supplier_actions'
      AND CONSTRAINT_NAME = 'fk_supplier_actions_supplier'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE supplier_actions ADD CONSTRAINT fk_supplier_actions_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT ''fk_supplier_actions_supplier already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- purchase_order_items.purchase_order_id -> purchase_orders(id)
-- Hibernate uses CascadeType.ALL + orphanRemoval so FK behaviour
-- must align: ON DELETE CASCADE.
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'purchase_order_items'
      AND CONSTRAINT_NAME = 'fk_po_items_purchase_order'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE purchase_order_items ADD CONSTRAINT fk_po_items_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT ''fk_po_items_purchase_order already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sale_invoice_items.sale_invoice_id -> sale_invoices(id)
-- Hibernate uses CascadeType.ALL + orphanRemoval: ON DELETE CASCADE.
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'PartsHub'
      AND TABLE_NAME   = 'sale_invoice_items'
      AND CONSTRAINT_NAME = 'fk_sale_items_sale_invoice'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE sale_invoice_items ADD CONSTRAINT fk_sale_items_sale_invoice FOREIGN KEY (sale_invoice_id) REFERENCES sale_invoices(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT ''fk_sale_items_sale_invoice already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- CHECK CONSTRAINTS  (MySQL 8.0.16+)
-- ============================================================

-- inventory_items: stock and price values must be non-negative
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'inventory_items'
      AND CONSTRAINT_NAME = 'chk_inventory_qty_non_negative'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE inventory_items ADD CONSTRAINT chk_inventory_qty_non_negative CHECK (qty_on_hand >= 0)',
    'SELECT ''chk_inventory_qty_non_negative already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'inventory_items'
      AND CONSTRAINT_NAME = 'chk_inventory_reorder_non_negative'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE inventory_items ADD CONSTRAINT chk_inventory_reorder_non_negative CHECK (reorder_level >= 0)',
    'SELECT ''chk_inventory_reorder_non_negative already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'inventory_items'
      AND CONSTRAINT_NAME = 'chk_inventory_unit_cost_non_negative'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE inventory_items ADD CONSTRAINT chk_inventory_unit_cost_non_negative CHECK (unit_cost >= 0)',
    'SELECT ''chk_inventory_unit_cost_non_negative already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'inventory_items'
      AND CONSTRAINT_NAME = 'chk_inventory_selling_price_non_negative'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE inventory_items ADD CONSTRAINT chk_inventory_selling_price_non_negative CHECK (selling_price >= 0)',
    'SELECT ''chk_inventory_selling_price_non_negative already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- purchase_order_items: quantities must be positive / non-negative
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'purchase_order_items'
      AND CONSTRAINT_NAME = 'chk_po_items_qty_ordered_positive'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE purchase_order_items ADD CONSTRAINT chk_po_items_qty_ordered_positive CHECK (qty_ordered > 0)',
    'SELECT ''chk_po_items_qty_ordered_positive already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'purchase_order_items'
      AND CONSTRAINT_NAME = 'chk_po_items_qty_received_non_negative'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE purchase_order_items ADD CONSTRAINT chk_po_items_qty_received_non_negative CHECK (qty_received >= 0)',
    'SELECT ''chk_po_items_qty_received_non_negative already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sale_invoice_items: quantity must be positive
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'sale_invoice_items'
      AND CONSTRAINT_NAME = 'chk_sale_items_qty_positive'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE sale_invoice_items ADD CONSTRAINT chk_sale_items_qty_positive CHECK (quantity > 0)',
    'SELECT ''chk_sale_items_qty_positive already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- suppliers: score must be in range 0-100
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'suppliers'
      AND CONSTRAINT_NAME = 'chk_suppliers_score_range'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE suppliers ADD CONSTRAINT chk_suppliers_score_range CHECK (score >= 0 AND score <= 100)',
    'SELECT ''chk_suppliers_score_range already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- promotional_campaign: end_date must not precede start_date
SET @constraint_exists = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA   = 'PartsHub'
      AND TABLE_NAME     = 'promotional_campaign'
      AND CONSTRAINT_NAME = 'chk_campaign_dates_valid'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE promotional_campaign ADD CONSTRAINT chk_campaign_dates_valid CHECK (end_date >= start_date)',
    'SELECT ''chk_campaign_dates_valid already exists'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'All constraints applied.' AS status;
