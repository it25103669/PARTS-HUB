-- ============================================================
-- PartsHub Pro  - 02_create_tables.sql
-- Database : PartsHub  |  Engine: MySQL 8.0
-- ============================================================
-- Creates all tables required by the Spring Boot / JPA entities.
-- Run AFTER 01_create_database.sql.
-- Tables are created in dependency order (parents before children).
-- All tables use IF NOT EXISTS so the script is idempotent.
-- ============================================================

USE PartsHub;

-- ============================================================
-- 1. USER ACCOUNTS  (no FK dependencies)
-- ============================================================
-- Entity : com.partshub.auth.entity.UserAccount
-- Table  : user_accounts
-- PK     : id BIGINT AUTO_INCREMENT
-- Auth   : SHA-256 password hash stored in password_hash
-- ============================================================
CREATE TABLE IF NOT EXISTS user_accounts (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)    NOT NULL,
    password_hash VARCHAR(255)   NOT NULL,
    full_name     VARCHAR(100)   NOT NULL,
    role          VARCHAR(50)    NOT NULL,
    student_id    VARCHAR(50)    NULL,
    title         VARCHAR(100)   NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. SUPPLIERS  (no FK dependencies)
-- ============================================================
-- Entity : com.partshub.supplier.entity.Supplier
-- Table  : suppliers
-- PK     : id BIGINT AUTO_INCREMENT
-- ============================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    supplier_code     VARCHAR(50)     NOT NULL,
    supplier_name     VARCHAR(150)    NOT NULL,
    category          VARCHAR(100)    NULL     DEFAULT 'General Hardware',
    tier              VARCHAR(50)     NULL     DEFAULT 'Tier 3 - Silver',
    orders_completed  INT             NULL     DEFAULT 0,
    score             INT             NOT NULL DEFAULT 75,
    on_time_rate      DECIMAL(5,2)    NULL     DEFAULT 95.00,
    defect_rate       DECIMAL(5,2)    NULL     DEFAULT 1.00,
    lead_time_days    INT             NULL     DEFAULT 5,
    status            VARCHAR(20)     NULL     DEFAULT 'ACTIVE',
    is_active         BIT(1)          NOT NULL DEFAULT 1,
    contact_phone     VARCHAR(50)     NULL,
    contact_email     VARCHAR(100)    NULL,
    address           VARCHAR(255)    NOT NULL DEFAULT 'Colombo, Western Province, Sri Lanka',
    created_at        DATETIME(6)     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. SUPPLIER PARTS  (FK -> suppliers)
-- ============================================================
-- Entity : com.partshub.supplier.entity.SupplierPart
-- Table  : supplier_parts
-- PK     : id BIGINT AUTO_INCREMENT
-- FK     : supplier_id -> suppliers(id)
-- Note   : supplier_id is stored as a plain Long (not a JPA
--          @ManyToOne join) so no Hibernate-managed FK exists
--          on this side; we add it manually for integrity.
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier_parts (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,
    supplier_id           BIGINT          NOT NULL,
    supplier_name         VARCHAR(150)    NULL,
    part_name             VARCHAR(200)    NOT NULL,
    sku_code              VARCHAR(100)    NOT NULL,
    category              VARCHAR(100)    NULL,
    supply_price          DECIMAL(12,2)   NOT NULL,
    lead_time_days        INT             NULL     DEFAULT 5,
    availability_status   VARCHAR(30)     NULL     DEFAULT 'AVAILABLE',
    notes                 VARCHAR(255)    NULL,
    created_at            DATETIME(6)     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. SUPPLIER ACTIONS  (FK -> suppliers)
-- ============================================================
-- Entity : com.partshub.supplier.entity.SupplierAction
-- Table  : supplier_actions
-- PK     : id BIGINT AUTO_INCREMENT
-- FK     : supplier_id -> suppliers(id)
-- Note   : supplier_id stored as plain Long (same pattern)
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier_actions (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_id      BIGINT       NOT NULL,
    action_category  VARCHAR(20)  NOT NULL,   -- POSITIVE or NEGATIVE
    action_name      VARCHAR(150) NOT NULL,
    score_impact     INT          NOT NULL,
    po_reference     VARCHAR(100) NULL,
    action_date      DATE         NOT NULL,
    created_at       DATETIME(6)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. INVENTORY ITEMS  (no FK dependencies in Java entity)
-- ============================================================
-- Entity : com.partshub.inventory.entity.InventoryItem
-- Table  : inventory_items
-- PK     : id BIGINT AUTO_INCREMENT
-- Note   : supplier_name is a denormalized string, not a FK.
--          Status is computed by @PrePersist/@PreUpdate in Java.
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_items (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    part_name           VARCHAR(200)    NOT NULL,
    sku_code            VARCHAR(100)    NOT NULL,
    category            VARCHAR(50)     NOT NULL,
    supplier_name       VARCHAR(150)    NOT NULL DEFAULT 'Tech Lanka Pvt Ltd',
    qty_on_hand         INT             NOT NULL DEFAULT 0,
    reorder_level       INT             NOT NULL DEFAULT 10,
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    selling_price       DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    warehouse_location  VARCHAR(100)    NOT NULL DEFAULT 'Colombo WH-A (Bin 01)',
    description         TEXT            NULL,
    image               VARCHAR(255)    NULL,
    status              VARCHAR(20)     NULL     DEFAULT 'IN_STOCK',
    created_at          DATETIME(6)     NULL,
    updated_at          DATETIME(6)     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. PURCHASE ORDERS  (no FK dependencies in Java entity)
-- ============================================================
-- Entity : com.partshub.purchase.entity.PurchaseOrder
-- Table  : purchase_orders
-- PK     : id BIGINT AUTO_INCREMENT
-- Note   : supplier_name is a denormalized string, not a FK.
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_orders (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    po_number               VARCHAR(50)     NOT NULL,
    supplier_name           VARCHAR(150)    NOT NULL,
    order_date              DATE            NOT NULL,
    expected_delivery_date  DATE            NOT NULL,
    total_amount            DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
    status                  VARCHAR(20)     NULL     DEFAULT 'SENT',
    notes                   TEXT            NULL,
    created_at              DATETIME(6)     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. PURCHASE ORDER ITEMS  (FK -> purchase_orders)
-- ============================================================
-- Entity : com.partshub.purchase.entity.PurchaseItem
-- Table  : purchase_order_items
-- PK     : id BIGINT AUTO_INCREMENT
-- FK     : purchase_order_id -> purchase_orders(id)  [Hibernate @ManyToOne]
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    purchase_order_id BIGINT          NOT NULL,
    part_name         VARCHAR(200)    NOT NULL,
    qty_ordered       INT             NOT NULL DEFAULT 1,
    qty_received      INT             NULL     DEFAULT 0,
    unit_cost         DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    line_total        DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
    condition_status  VARCHAR(50)     NULL     DEFAULT 'GOOD',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. SALE INVOICES  (no FK dependencies in Java entity)
-- ============================================================
-- Entity : com.partshub.sales.entity.SaleInvoice
-- Table  : sale_invoices
-- PK     : id BIGINT AUTO_INCREMENT
-- Note   : customer_name is a denormalized string.
--          Totals are calculated in Java (recalculateTotals()).
-- ============================================================
CREATE TABLE IF NOT EXISTS sale_invoices (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    invoice_number   VARCHAR(50)     NOT NULL,
    customer_name    VARCHAR(150)    NOT NULL,
    customer_contact VARCHAR(100)    NULL,
    sale_date        DATE            NOT NULL,
    payment_method   VARCHAR(50)     NOT NULL DEFAULT 'Cash',
    item_count       INT             NULL     DEFAULT 0,
    subtotal         DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
    discount_amount  DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
    tax_amount       DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
    grand_total      DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
    payment_status   VARCHAR(20)     NULL     DEFAULT 'PAID',
    created_at       DATETIME(6)     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. SALE INVOICE ITEMS  (FK -> sale_invoices)
-- ============================================================
-- Entity : com.partshub.sales.entity.SaleItem
-- Table  : sale_invoice_items
-- PK     : id BIGINT AUTO_INCREMENT
-- FK     : sale_invoice_id -> sale_invoices(id)  [Hibernate @ManyToOne]
-- ============================================================
CREATE TABLE IF NOT EXISTS sale_invoice_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    sale_invoice_id BIGINT          NOT NULL,
    part_name       VARCHAR(200)    NOT NULL,
    sku_code        VARCHAR(100)    NULL,
    quantity        INT             NOT NULL DEFAULT 1,
    unit_price      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    line_total      DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. RETURN CLAIMS  (no FK in Java entity - uses string refs)
-- ============================================================
-- Entity : com.partshub.returns.entity.ReturnClaim
-- Table  : return_claims
-- PK     : id BIGINT AUTO_INCREMENT
-- Note   : reference_number stores INV-XXXX or PO-XXXX as text.
--          No JPA FK relationships in this entity.
-- ============================================================
CREATE TABLE IF NOT EXISTS return_claims (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    claim_number     VARCHAR(50)  NOT NULL,
    claim_type       VARCHAR(30)  NOT NULL,   -- CUSTOMER_RETURN, SUPPLIER_RETURN, WARRANTY_CLAIM
    reference_number VARCHAR(50)  NOT NULL,   -- INV-XXXX or PO-XXXX
    part_name        VARCHAR(200) NOT NULL,
    party_name       VARCHAR(150) NULL,
    reason           VARCHAR(100) NOT NULL,
    resolution       VARCHAR(50)  NOT NULL,   -- Replace, Refund, Repair, Pending Review
    diagnostic_notes TEXT         NULL,
    status           VARCHAR(20)  NULL        DEFAULT 'PENDING',   -- PENDING, APPROVED, RESOLVED, REJECTED
    created_at       DATETIME(6)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11. PROMOTIONAL CAMPAIGN  (no FK dependencies)
-- ============================================================
-- Entity : com.partshub.reporting.campaign.entity.PromotionalCampaign
-- Table  : promotional_campaign  (singular - matches @Table annotation)
-- PK     : campaign_id BIGINT AUTO_INCREMENT  (@Column(name="campaign_id"))
-- Note   : status is an enum stored as VARCHAR: UPCOMING,ACTIVE,EXPIRED,CANCELLED
--          selected_products is a comma-separated TEXT field (future ManyToMany TODO)
-- ============================================================
CREATE TABLE IF NOT EXISTS promotional_campaign (
    campaign_id          BIGINT          NOT NULL AUTO_INCREMENT,
    campaign_name        VARCHAR(200)    NOT NULL,
    description          TEXT            NULL,
    selected_products    TEXT            NOT NULL,
    discount_percentage  DECIMAL(5,2)    NOT NULL,
    start_date           DATE            NOT NULL,
    end_date             DATE            NOT NULL,
    status               ENUM('UPCOMING','ACTIVE','EXPIRED','CANCELLED') NOT NULL,
    created_at           DATETIME(6)     NOT NULL,
    updated_at           DATETIME(6)     NULL,
    PRIMARY KEY (campaign_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'All tables created (or already exist).' AS status;
