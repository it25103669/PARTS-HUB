-- ============================================================
-- PartsHub Pro - Seed Data (matching UI templates)
-- ============================================================

-- 1. Suppliers
INSERT INTO suppliers (supplier_code, supplier_name, category, tier, orders_completed, score, on_time_rate, defect_rate, status, contact_phone, contact_email)
VALUES 
('SUP-LK-0101', 'Tech Lanka Pvt Ltd', 'CPUs & Motherboards', 'Tier 1 - Platinum', 142, 96, 99.2, 0.4, 'ACTIVE', '+94 11 234 5678', 'procurement@techlanka.lk'),
('SUP-LK-0245', 'PC World Suppliers', 'GPUs & Power Supplies', 'Tier 1 - Platinum', 118, 91, 97.5, 0.8, 'ACTIVE', '+94 11 345 6789', 'sales@pcworldsuppliers.lk'),
('SUP-LK-0312', 'Colombo Micro Hub', 'Memory & Storage SSDs', 'Tier 2 - Gold', 94, 88, 95.0, 1.2, 'ACTIVE', '+94 11 456 7890', 'orders@colombomicro.lk'),
('SUP-LK-0518', 'Nanotech Hardware Distributors', 'Cooling & Chassis', 'Tier 2 - Gold', 86, 82, 93.8, 1.5, 'ACTIVE', '+94 11 567 8901', 'supply@nanotech.lk'),
('SUP-LK-0089', 'ABC Computers', 'Peripherals & Cables', 'Tier 3 - Silver', 73, 78, 91.2, 2.1, 'ACTIVE', '+94 11 678 9012', 'info@abccomputers.lk'),
('SUP-LK-0420', 'Silicon Lanka Components', 'Discrete Electronics', 'Tier 3 - Silver', 52, 68, 86.4, 3.4, 'ACTIVE', '+94 11 789 0123', 'trade@siliconlanka.lk'),
('SUP-LK-0677', 'Lanka Chipset Logistics', 'OEM Processors', 'Tier 4 - Bronze', 39, 58, 79.1, 4.8, 'ACTIVE', '+94 11 890 1234', 'logistics@chipset.lk'),
('SUP-LK-0199', 'Apex Memory Systems', 'Legacy DRAM Modules', 'Tier 4 - Bronze', 28, 42, 68.5, 6.2, 'INACTIVE', '+94 11 901 2345', 'support@apexmemory.lk');

-- 2. Inventory Items
INSERT INTO inventory_items (part_name, sku_code, category, brand, compatibility, supplier_name, qty_on_hand, reorder_level, unit_cost, selling_price, warehouse_location, description, status, image, approval_status)
VALUES 
('Intel Core i9-14900K Processor', 'CPU-INT-1490K', 'CPU', 'Intel', 'LGA1700 Motherboards', 'Tech Lanka Pvt Ltd', 48, 15, 95000.00, 110000.00, 'Colombo WH-A (Bin 04)', '24 cores (8 P-cores + 16 E-cores), up to 6.0 GHz, LGA1700 box', 'IN_STOCK', 'Intel Core i9-14900K Processor.jpg', 'APPROVED'),
('ASUS ROG Strix RTX 4080 OC 16GB', 'GPU-ASUS-4080', 'GPU', 'ASUS', 'PCIe 4.0 / 5.0 x16', 'PC World Suppliers', 4, 10, 320000.00, 370000.00, 'Colombo WH-A (Bin 02)', '16GB GDDR6X, Axial-tech fans, 3.5-slot design, Dual BIOS', 'LOW_STOCK', 'ASUS ROG Strix RTX 4080 OC 16GB.jpg', 'APPROVED'),
('Corsair Vengeance DDR5 32GB (2x16GB) 6000MHz', 'RAM-COR-32G6K', 'RAM', 'Corsair', 'DDR5 Compatible Motherboards', 'Colombo Micro Hub', 112, 25, 26000.00, 32000.00, 'Colombo WH-B (Rack 01)', 'Intel XMP 3.0 optimized, aluminum heatspreader, CL36', 'IN_STOCK', 'Corsair Vengeance DDR5 32GB (2x16GB) 6000MHz.jpg', 'APPROVED'),
('MSI MAG Z790 Tomahawk WiFi Motherboard', 'MB-MSI-Z790T', 'Motherboard', 'MSI', 'LGA1700 / Intel 12th/13th/14th Gen', 'Tech Lanka Pvt Ltd', 26, 12, 72000.00, 90000.00, 'Colombo WH-A (Bin 08)', 'LGA 1700, DDR5, PCIe 5.0, Wi-Fi 6E, 2.5GbE LAN', 'IN_STOCK', 'MSI MAG Z790 Tomahawk WiFi Motherboard.jpg', 'APPROVED'),
('Samsung 990 PRO 2TB NVMe M.2 SSD', 'SSD-SAM-990P2', 'Storage', 'Samsung', 'M.2 NVMe PCIe 4.0', 'Colombo Micro Hub', 64, 20, 48000.00, 58000.00, 'Colombo WH-B (Rack 03)', 'PCIe 4.0 NVMe, read up to 7450 MB/s, write up to 6900 MB/s', 'IN_STOCK', 'Samsung 990 PRO 2TB NVMe M.2 SSD.jpg', 'APPROVED'),
('Corsair RM850x 850W Fully Modular PSU', 'PSU-COR-850X', 'PSU', 'Corsair', 'ATX Standard Cases', 'PC World Suppliers', 18, 10, 38000.00, 46000.00, 'Colombo WH-A (Bin 11)', '80 PLUS Gold certified, Zero RPM fan mode, 100% Japanese capacitors', 'IN_STOCK', 'Corsair RM850x 850W Fully Modular PSU.jpg', 'APPROVED'),
('Corsair RM850x 850W Gold PSU', 'PSU-COR-850G', 'PSU', 'Corsair', 'ATX Standard Cases', 'PC World Suppliers', 12, 5, 36000.00, 44000.00, 'Colombo WH-A (Bin 12)', '80 PLUS Gold certified power supply unit', 'IN_STOCK', 'Corsair RM850x 850W Gold PSU.jpg', 'APPROVED'),
('NZXT Kraken Elite 360 RGB AIO Cooler', 'CLR-NZXT-K360', 'Cooling', 'NZXT', 'LGA1700 / AM5 / AM4', 'Nanotech Hardware Distributors', 0, 8, 75000.00, 89000.00, 'Kandy Regional Hub (Bin 03)', '360mm radiator, 2.36" wide-angle LCD display, 3x F120 RGB Core fans', 'OUT_OF_STOCK', 'NZXT Kraken Elite 360 RGB AIO Cooler.jpg', 'APPROVED'),
('Logitech G Pro X Superlight 2 Wireless Mouse', 'PER-LOG-GPX2', 'Peripherals', 'Logitech', 'USB / Wireless 2.4GHz', 'ABC Computers', 35, 15, 34000.00, 42000.00, 'Galle Depot (Rack 05)', 'HERO 2 sensor 32K DPI, LIGHTFORCE hybrid switches, 60g ultralight', 'IN_STOCK', 'Logitech G Pro X Superlight 2 Wireless Mouse.jpg', 'APPROVED'),
('AMD Ryzen 7 9800X3D Processor', 'CPU-AMD-9800X3D', 'CPU', 'AMD', 'AM5 Motherboards', 'Tech Lanka Pvt Ltd', 15, 5, 145000.00, 168000.00, 'Colombo WH-A (Bin 05)', '8 cores / 16 threads 3D V-Cache gaming processor', 'IN_STOCK', 'AMD Ryzen 7 9800X3D Processor.jpg', 'APPROVED'),
('AMD Ryzen 9 7950X Processor', 'CPU-AMD-7950X', 'CPU', 'AMD', 'AM5 Motherboards', 'Tech Lanka Pvt Ltd', 20, 5, 160000.00, 185000.00, 'Colombo WH-A (Bin 06)', '16 cores / 32 threads high performance CPU', 'IN_STOCK', 'AMD Ryzen 9 7950X Processor.jpg', 'APPROVED'),
('Gigabyte RTX 4070 Ti Super 16GB (Rev 022639)', 'GPU-GIG-4070TIS-022639', 'GPU', 'Gigabyte', 'PCIe 4.0 x16', 'PC World Suppliers', 8, 3, 275000.00, 310000.00, 'Colombo WH-A (Bin 03)', '16GB GDDR6X, WINDFORCE cooling system, Ref: 022639', 'IN_STOCK', 'Gigabyte RTX 4070 Ti Super 16GB - 022639.jpg', 'APPROVED'),
('Gigabyte RTX 4070 Ti Super 16GB (Rev 022720)', 'GPU-GIG-4070TIS-022720', 'GPU', 'Gigabyte', 'PCIe 4.0 x16', 'PC World Suppliers', 6, 3, 275000.00, 310000.00, 'Colombo WH-A (Bin 03)', '16GB GDDR6X, WINDFORCE cooling system, Ref: 022720', 'IN_STOCK', 'Gigabyte RTX 4070 Ti Super 16GB - 022720.jpg', 'APPROVED'),
('Gigabyte RTX 4070 Ti Super 16GB', 'GPU-GIG-4070TIS', 'GPU', 'Gigabyte', 'PCIe 4.0 x16', 'PC World Suppliers', 10, 3, 270000.00, 305000.00, 'Colombo WH-A (Bin 03)', '16GB GDDR6X, Triple fan cooling', 'IN_STOCK', 'Gigabyte RTX 4070 Ti Super 16GB.jpg', 'APPROVED');

-- 3. Purchase Orders & Line Items
INSERT INTO purchase_orders (po_number, supplier_name, order_date, expected_delivery_date, total_amount, status, notes)
VALUES 
('PO-2026-8812', 'Tech Lanka Pvt Ltd', '2026-09-15', '2026-09-22', 2200000.00, 'SENT', 'Deliver to Colombo WH-A. Priority air express dispatch.'),
('PO-2026-8809', 'PC World Suppliers', '2026-09-12', '2026-09-18', 3700000.00, 'RECEIVED', 'All 10x RTX 4080 cards received in factory sealed condition.'),
('PO-2026-8795', 'Colombo Micro Hub', '2026-09-10', '2026-09-16', 1560000.00, 'RECEIVED', '60 units Samsung SSD received and barcode scanned.'),
('PO-2026-8780', 'Nanotech Hardware Distributors', '2026-09-08', '2026-09-14', 890000.00, 'PENDING', 'Awaiting customs port clearance verification.'),
('PO-2026-8762', 'ABC Computers', '2026-09-02', '2026-09-08', 540000.00, 'RECEIVED', 'Logitech peripherals verified and stocked in Galle depot.'),
('PO-2026-8740', 'Apex Memory Systems', '2026-08-25', '2026-09-01', 320000.00, 'CANCELLED', 'Cancelled due to repeated supplier shipping delays.');

INSERT INTO purchase_order_items (purchase_order_id, part_name, qty_ordered, qty_received, unit_cost, line_total, condition_status)
VALUES 
(1, 'Intel Core i9-14900K Processor', 20, 0, 110000.00, 2200000.00, 'GOOD'),
(2, 'ASUS ROG Strix RTX 4080 OC 16GB', 10, 10, 370000.00, 3700000.00, 'GOOD'),
(3, 'Samsung 990 PRO 2TB NVMe M.2 SSD', 30, 30, 52000.00, 1560000.00, 'GOOD'),
(4, 'NZXT Kraken Elite 360 RGB AIO Cooler', 10, 0, 89000.00, 890000.00, 'GOOD');

-- 4. Sale Invoices & Line Items
INSERT INTO sale_invoices (invoice_number, customer_name, customer_contact, sale_date, payment_method, item_count, subtotal, discount_amount, tax_amount, grand_total, payment_status)
VALUES 
('INV-2026-1049', 'Lanka Tech Solutions Pvt Ltd', '+94 11 234 5678', '2026-09-17', 'Bank Transfer', 4, 250000.00, 12500.00, 42750.00, 280250.00, 'PAID'),
('INV-2026-1048', 'Colombo PC Clinic', '+94 11 345 6789', '2026-09-16', 'Cash', 2, 180000.00, 9000.00, 30780.00, 201780.00, 'PAID'),
('INV-2026-1047', 'Apex Gaming Esports Arena', '+94 11 456 7890', '2026-09-15', '30-Day Credit', 6, 740000.00, 37000.00, 126540.00, 829540.00, 'PENDING'),
('INV-2026-1046', 'Walk-in Retail Client', '+94 77 123 4567', '2026-09-15', 'Credit Card', 1, 42000.00, 0.00, 7560.00, 49560.00, 'PAID'),
('INV-2026-1045', 'Southern Silicon Works', '+94 11 567 8901', '2026-09-14', 'Bank Transfer', 3, 310000.00, 15500.00, 53010.00, 347510.00, 'OVERDUE'),
('INV-2026-1044', 'Kandy Tech Hub', '+94 11 678 9012', '2026-09-12', 'Bank Transfer', 2, 148000.00, 7400.00, 25308.00, 165908.00, 'PAID');

INSERT INTO sale_invoice_items (sale_invoice_id, part_name, quantity, unit_price, line_total)
VALUES 
(1, 'Intel Core i9-14900K Processor', 2, 125000.00, 250000.00),
(2, 'MSI MAG Z790 Tomahawk WiFi Motherboard', 2, 90000.00, 180000.00),
(3, 'ASUS ROG Strix RTX 4080 OC 16GB', 2, 370000.00, 740000.00),
(4, 'Logitech G Pro X Superlight 2 Wireless Mouse', 1, 42000.00, 42000.00);

-- 5. Return Claims
INSERT INTO return_claims (claim_number, claim_type, reference_number, part_name, party_name, reason, resolution, diagnostic_notes, status)
VALUES 
('CLM-2026-0421', 'CUSTOMER_RETURN', 'INV-2026-1049', 'Intel Core i9-14900K', 'Lanka Tech Solutions', 'Defective / Non-Functional', 'Replace', 'BSOD under synthetic Prime95 load. Pin integrity checked.', 'PENDING'),
('CLM-2026-0418', 'SUPPLIER_RETURN', 'PO-2026-8809', 'ASUS ROG Strix RTX 4080', 'PC World Suppliers', 'Damaged in Transit', 'Refund', 'Box crushed during transit, thermal shroud bent.', 'APPROVED'),
('CLM-2026-0402', 'WARRANTY_CLAIM', 'INV-2026-1012', 'Corsair RM850x 850W PSU', 'Colombo PC Clinic', 'Coil Whine', 'Repair', 'Relayed to authorized Corsair RMA depot in Colombo.', 'RESOLVED'),
('CLM-2026-0410', 'CUSTOMER_RETURN', 'INV-2026-1033', 'Samsung 990 PRO 2TB SSD', 'Walk-in Retail Client', 'Wrong Item', 'Refund', 'Customer meant to purchase 4TB variant. Seal was opened.', 'REJECTED'),
('CLM-2026-0425', 'WARRANTY_CLAIM', 'INV-2026-1047', 'MSI MAG Z790 Tomahawk', 'Apex Gaming Hub', 'Defective', 'Pending Review', 'Memory channel B non-responsive with test DDR5 modules.', 'PENDING'),
('CLM-2026-0395', 'SUPPLIER_RETURN', 'PO-2026-8762', 'Logitech G Pro X Superlight 2', 'ABC Computers', 'Defective', 'Replace', 'Scroll encoder bouncing. Batch replacement authorized.', 'RESOLVED');

-- 6. Promotional Campaigns
INSERT INTO promotional_campaign (campaign_name, description, selected_products, discount_percentage, start_date, end_date, status)
VALUES 
('Q3 Creator & AI Powerhouse Fest', 'Special discount campaign targeting top workstation hardware', 'Intel Core i9-14900K Processor,ASUS ROG Strix RTX 4080 OC 16GB,Corsair Vengeance DDR5 32GB', 12.50, '2026-09-01', '2026-09-30', 'ACTIVE'),
('Back-to-Campus PC Upgrade Blitz', 'Entry to mid tier CPU and RAM student specials', 'Samsung 990 PRO 2TB NVMe M.2 SSD,Logitech G Pro X Superlight 2 Wireless Mouse', 8.00, '2026-10-01', '2026-10-20', 'UPCOMING');

-- 7. User Accounts
INSERT INTO user_accounts (username, password_hash, full_name, role, student_id, title)
VALUES 
('perera', '1ad150cbba1393d7a5c1a354d6e1ca7883c1a86a8ac7e0200b7ebf8670a97820', 'Perera K.P.S.C.', 'ROLE_STOREKEEPER', 'IT25102622', 'Inventory Management Officer'),
('yuresh', '6cc135c61374518ffb592f420ae50c90a8b4b6bd37515b5782d3f8c4e9984cf6', 'Yuresh D.D.A.', 'ROLE_PROCUREMENT_OFFICER', 'IT25103669', 'Supplier Management Officer'),
('desilva', '7d1514cb07211bc80c85193b760fb65a504345d4bd63b097f6eb53b3de588fe8', 'De Silva T.S.A.I.A', 'ROLE_PURCHASING_AGENT', 'IT25101669', 'Purchase & Receiving Manager'),
('thamilalagan', 'edc3613735a84f57cafee441cfc4332de2b6827167c997447552065fdda501ac', 'Thamilalagan N.', 'ROLE_SALES_REP', 'IT25100888', 'Sales & Billing Representative'),
('indrathilake', '9b693d389400f2746ab7d409909ef394ec7656bf5f5fec73d7a2fd27e4063a74', 'Indrathilake H.W.S.M', 'ROLE_BUSINESS_OWNER', 'IT25100755', 'Executive Business Owner'),
('premachandra', 'b050cd90ef19109988d4bd8f61d063815bd5f1156178ab6d05ecea988ee6d48e', 'Premachandra T.G.D.T', 'ROLE_RETURNS_CLERK', 'IT25101790', 'Returns & Warranty Officer');

-- 8. Supplier Linked Spare Parts
INSERT INTO supplier_parts (supplier_id, supplier_name, part_name, sku_code, category, supply_price, lead_time_days, availability_status, notes)
VALUES
(1, 'Tech Lanka Pvt Ltd', 'Intel Core i9-14900K Processor', 'CPU-INT-1490K', 'CPU', 95000.00, 3, 'AVAILABLE', 'Authorized Intel Regional Distributor'),
(1, 'Tech Lanka Pvt Ltd', 'MSI MAG Z790 Tomahawk WiFi Motherboard', 'MB-MSI-Z790T', 'Motherboard', 72000.00, 4, 'AVAILABLE', 'Factory sealed motherboard units'),
(2, 'PC World Suppliers', 'ASUS ROG Strix RTX 4080 OC 16GB', 'GPU-ASUS-4080', 'GPU', 320000.00, 5, 'AVAILABLE', 'Priority allocation from ASUS Taiwan'),
(2, 'PC World Suppliers', 'Corsair RM850x 850W Fully Modular PSU', 'PSU-COR-850X', 'PSU', 38000.00, 3, 'AVAILABLE', 'Gold rated units with 10y warranty'),
(3, 'Colombo Micro Hub', 'Corsair Vengeance DDR5 32GB (2x16GB) 6000MHz', 'RAM-COR-32G6K', 'RAM', 26000.00, 2, 'AVAILABLE', 'High-speed DDR5 kits'),
(3, 'Colombo Micro Hub', 'Samsung 990 PRO 2TB NVMe M.2 SSD', 'SSD-SAM-990P2', 'Storage', 48000.00, 2, 'AVAILABLE', 'PCIe 4.0 high speed SSDs'),
(4, 'Nanotech Hardware Distributors', 'NZXT Kraken Elite 360 RGB AIO Cooler', 'CLR-NZXT-K360', 'Cooling', 75000.00, 4, 'AVAILABLE', 'Liquid cooling kits'),
(5, 'ABC Computers', 'Logitech G Pro X Superlight 2 Wireless Mouse', 'PER-LOG-GPX2', 'Peripherals', 34000.00, 3, 'AVAILABLE', 'Esports grade wireless mice');

