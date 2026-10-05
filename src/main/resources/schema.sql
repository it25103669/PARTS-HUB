-- ============================================================
-- PartsHub Pro - Enterprise MySQL Database Schema
-- Database: PartsHub
-- ============================================================

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_name VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    tier VARCHAR(50) DEFAULT 'Standard',
    orders_completed INT DEFAULT 0,
    score INT DEFAULT 75,
    on_time_rate DECIMAL(5,2) DEFAULT 95.00,
    defect_rate DECIMAL(5,2) DEFAULT 1.00,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_active BOOLEAN DEFAULT TRUE,
    lead_time_days INT DEFAULT 5,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(100),
    address VARCHAR(255) DEFAULT 'Colombo, Western Province, Sri Lanka',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS supplier_parts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(150),
    part_name VARCHAR(200) NOT NULL,
    sku_code VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    supply_price DECIMAL(12,2) NOT NULL,
    lead_time_days INT DEFAULT 5,
    availability_status VARCHAR(30) DEFAULT 'AVAILABLE',
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_supplier_parts_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS supplier_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    action_category VARCHAR(20) NOT NULL,
    action_name VARCHAR(150) NOT NULL,
    score_impact INT NOT NULL,
    po_reference VARCHAR(100),
    action_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_supplier_actions_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS inventory_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_name VARCHAR(200) NOT NULL,
    sku_code VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    brand VARCHAR(100) DEFAULT 'OEM / Generic',
    compatibility VARCHAR(200) DEFAULT 'Universal PC Standard',
    supplier_name VARCHAR(150) NOT NULL,
    qty_on_hand INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    unit_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    selling_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    warehouse_location VARCHAR(100) NOT NULL,
    description TEXT,
    image VARCHAR(255),
    status VARCHAR(20) DEFAULT 'IN_STOCK',
    warranty_period_months INT DEFAULT 12,
    approval_status VARCHAR(20) DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_name VARCHAR(150) NOT NULL,
    order_date DATE NOT NULL,
    expected_delivery_date DATE NOT NULL,
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'SENT',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    part_name VARCHAR(200) NOT NULL,
    qty_ordered INT NOT NULL,
    qty_received INT DEFAULT 0,
    unit_cost DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(14,2) NOT NULL,
    condition_status VARCHAR(50) DEFAULT 'GOOD',
    CONSTRAINT fk_po_items_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sale_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(150) NOT NULL,
    customer_contact VARCHAR(100),
    sale_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    item_count INT DEFAULT 0,
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) DEFAULT 'PAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sale_invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_invoice_id BIGINT NOT NULL,
    part_name VARCHAR(200) NOT NULL,
    sku_code VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_sale_items_invoice FOREIGN KEY (sale_invoice_id) REFERENCES sale_invoices(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS return_claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_number VARCHAR(50) NOT NULL UNIQUE,
    claim_type VARCHAR(30) NOT NULL,
    reference_number VARCHAR(50) NOT NULL,
    part_name VARCHAR(200) NOT NULL,
    party_name VARCHAR(150),
    quantity INT NOT NULL DEFAULT 1,
    item_condition VARCHAR(50) DEFAULT 'DEFECTIVE',
    reason VARCHAR(100) NOT NULL,
    resolution VARCHAR(50) NOT NULL,
    diagnostic_notes TEXT,
    sale_date DATE,
    warranty_period_months INT DEFAULT 12,
    warranty_expiry_date DATE,
    in_warranty BOOLEAN DEFAULT TRUE,
    goodwill_override BOOLEAN DEFAULT FALSE,
    goodwill_reason VARCHAR(255),
    rejection_reason VARCHAR(255),
    supplier_name VARCHAR(150),
    supplier_claim_status VARCHAR(50) DEFAULT 'NOT_APPLICABLE',
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS promotional_campaign (
    campaign_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_name VARCHAR(150) NOT NULL,
    description TEXT,
    selected_products TEXT,
    discount_percentage DECIMAL(5,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    student_id VARCHAR(50),
    title VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_role VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'INFO',
    module VARCHAR(50) DEFAULT 'system',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS saved_report_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    report_type VARCHAR(50) NOT NULL,
    date_range_type VARCHAR(50) NOT NULL,
    custom_start_date DATE,
    custom_end_date DATE,
    product_filter VARCHAR(255),
    category_filter VARCHAR(255),
    supplier_filter VARCHAR(255),
    chart_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

