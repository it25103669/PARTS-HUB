package com.partshub;

import com.partshub.inventory.entity.InventoryItem;
import com.partshub.inventory.service.InventoryService;
import com.partshub.purchase.entity.PurchaseItem;
import com.partshub.purchase.entity.PurchaseOrder;
import com.partshub.purchase.service.PurchaseService;
import com.partshub.sales.entity.SaleInvoice;
import com.partshub.sales.entity.SaleItem;
import com.partshub.sales.service.SalesService;
import com.partshub.supplier.entity.Supplier;
import com.partshub.supplier.entity.SupplierAction;
import com.partshub.supplier.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class PartshubApplicationTests {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SalesService salesService;

    @Autowired
    private com.partshub.returns.service.ReturnsService returnsService;

    @Autowired
    private com.partshub.reporting.service.DashboardService dashboardService;

    @Autowired
    private com.partshub.supplier.controller.SupplierController supplierController;

    @Test
    void contextLoads() {
        assertNotNull(inventoryService);
        assertNotNull(supplierService);
        assertNotNull(purchaseService);
        assertNotNull(salesService);
        assertNotNull(returnsService);
        assertNotNull(dashboardService);
        assertNotNull(supplierController);
    }

    @Test
    void testInventoryCrud() {
        InventoryItem item = new InventoryItem();
        item.setPartName("Test CPU");
        item.setSkuCode("TEST-CPU-01");
        item.setCategory("CPU");
        item.setSupplierName("Tech Lanka Pvt Ltd");
        item.setWarehouseLocation("Colombo WH-A (Bin 04)");
        item.setQtyOnHand(20);
        item.setReorderLevel(5);
        item.setUnitCost(new BigDecimal("50000"));
        item.setSellingPrice(new BigDecimal("60000"));
        item.setStatus("IN_STOCK");

        InventoryItem saved = inventoryService.createItem(item);
        assertNotNull(saved.getId());
        assertEquals("Test CPU", saved.getPartName());

        saved.setQtyOnHand(25);
        InventoryItem updated = inventoryService.updateItem(saved.getId(), saved);
        assertEquals(25, updated.getQtyOnHand());

        inventoryService.deleteItem(saved.getId());
        assertTrue(inventoryService.getItemById(saved.getId()).isEmpty());
    }

    @Test
    void testSupplierFullCrudAndScoring() {
        // 1. Create
        Supplier supplier = new Supplier();
        supplier.setSupplierCode("SUP-TEST-01");
        supplier.setSupplierName("Test Distributor");
        supplier.setCategory("RAM");
        supplier.setOrdersCompleted(10);
        supplier.setScore(80);
        supplier.setOnTimeRate(new BigDecimal("90.0"));
        supplier.setDefectRate(new BigDecimal("1.0"));
        supplier.setStatus("ACTIVE");
        supplier.setContactEmail("test@distributor.lk");
        supplier.setContactPhone("+94 11 000 1111");
        supplier.setAddress("123 Galle Road, Colombo 03");
        supplier.setLeadTimeDays(4);

        Supplier saved = supplierService.createSupplier(supplier);
        assertNotNull(saved.getId());
        assertEquals("Test Distributor", saved.getSupplierName());
        assertEquals("Tier 2 - Gold", saved.getTier());

        // 2. Read
        assertTrue(supplierService.getSupplierById(saved.getId()).isPresent());
        assertEquals("Test Distributor", supplierService.getSupplierById(saved.getId()).get().getSupplierName());

        // 3. Update details
        saved.setSupplierName("Test Distributor Updated");
        saved.setCategory("CPUs & Motherboards");
        Supplier updated = supplierService.updateSupplier(saved.getId(), saved);
        assertEquals("Test Distributor Updated", updated.getSupplierName());
        assertEquals("CPUs & Motherboards", updated.getCategory());

        // 4. Performance update (Record Action)
        SupplierAction action = new SupplierAction();
        action.setActionCategory("POSITIVE");
        action.setActionName("On-Time Bulk Delivery");
        action.setScoreImpact(5);
        action.setPoReference("PO-TEST-100");

        Supplier ranked = supplierService.recordAction(saved.getId(), action);
        assertTrue(ranked.getScore() > 80);
        assertFalse(supplierService.getSupplierActions(saved.getId()).isEmpty());

        // 5. Delete (and verify cascade delete of actions)
        supplierService.deleteSupplier(saved.getId());
        assertTrue(supplierService.getSupplierById(saved.getId()).isEmpty());
        assertTrue(supplierService.getSupplierActions(saved.getId()).isEmpty());
    }

    @Test
    void testSupplierControllerEndpoints() {
        // Controller Create
        Supplier supplier = new Supplier();
        supplier.setSupplierName("Controller Test Supplier");
        supplier.setSupplierCode("SUP-CTRL-9999");
        supplier.setCategory("Storage");
        supplier.setStatus("ACTIVE");
        supplier.setContactEmail("ctrl@testvendor.lk");
        supplier.setContactPhone("+94 11 999 8888");
        supplier.setAddress("55 Marine Drive, Colombo 03");
        supplier.setLeadTimeDays(5);

        var createResp = (org.springframework.http.ResponseEntity<?>) supplierController.createSupplier(supplier, null, "application/json");
        assertEquals(org.springframework.http.HttpStatus.CREATED, createResp.getStatusCode());
        Supplier created = (Supplier) createResp.getBody();
        assertNotNull(created);
        assertNotNull(created.getId());

        // Controller Read
        var getResp = supplierController.getSupplierById(created.getId());
        assertEquals(org.springframework.http.HttpStatus.OK, getResp.getStatusCode());

        // Controller Update
        created.setSupplierName("Controller Test Supplier Updated");
        var updateResp = supplierController.updateSupplierPut(created.getId(), created);
        assertEquals(org.springframework.http.HttpStatus.OK, updateResp.getStatusCode());
        assertEquals("Controller Test Supplier Updated", ((Supplier) updateResp.getBody()).getSupplierName());

        // Controller Action
        SupplierAction action = new SupplierAction();
        action.setActionCategory("POSITIVE");
        action.setActionName("Fast response (+3)");
        action.setScoreImpact(3);
        action.setPoReference("PO-CTRL-01");
        var actionResp = supplierController.recordAction(created.getId(), action);
        assertEquals(org.springframework.http.HttpStatus.OK, actionResp.getStatusCode());

        // Controller Delete
        var delResp = supplierController.deleteSupplier(created.getId());
        assertEquals(org.springframework.http.HttpStatus.OK, delResp.getStatusCode());
    }

    @Test
    void testSupplierDuplicatePrevention() {
        Supplier s1 = new Supplier();
        s1.setSupplierCode("SUP-DUP-01");
        s1.setSupplierName("Duplicate Test Co");
        s1.setContactEmail("duptest@vendor.lk");
        s1.setAddress("10 Kandy Road, Kiribathgoda");
        Supplier saved = supplierService.createSupplier(s1);
        assertNotNull(saved.getId());

        try {
            // Duplicate Code
            Supplier s2 = new Supplier();
            s2.setSupplierCode("SUP-DUP-01");
            s2.setSupplierName("Different Name");
            s2.setContactEmail("diff@vendor.lk");
            s2.setAddress("Colombo");
            assertThrows(IllegalArgumentException.class, () -> supplierService.createSupplier(s2));

            // Duplicate Name
            Supplier s3 = new Supplier();
            s3.setSupplierCode("SUP-DUP-02");
            s3.setSupplierName("Duplicate Test Co");
            s3.setContactEmail("diff2@vendor.lk");
            s3.setAddress("Colombo");
            assertThrows(IllegalArgumentException.class, () -> supplierService.createSupplier(s3));

            // Duplicate Email
            Supplier s4 = new Supplier();
            s4.setSupplierCode("SUP-DUP-03");
            s4.setSupplierName("Another Vendor");
            s4.setContactEmail("duptest@vendor.lk");
            s4.setAddress("Colombo");
            assertThrows(IllegalArgumentException.class, () -> supplierService.createSupplier(s4));
        } finally {
            supplierService.deleteSupplier(saved.getId());
        }
    }

    @Test
    void testSupplierValidationRejections() {
        Supplier invalid = new Supplier();
        invalid.setSupplierName(""); // Blank name
        invalid.setSupplierCode("INVALID CODE WITH SPACES"); // Invalid pattern
        invalid.setContactEmail("not-an-email"); // Invalid email
        invalid.setAddress(""); // Blank address

        var response = (org.springframework.http.ResponseEntity<?>) supplierController.createSupplier(invalid, null, "application/json");
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("fieldErrors"));
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) body.get("fieldErrors");
        assertTrue(fieldErrors.containsKey("supplierName"));
        assertTrue(fieldErrors.containsKey("supplierCode"));
        assertTrue(fieldErrors.containsKey("contactEmail"));
        assertTrue(fieldErrors.containsKey("address"));
    }

    @Test
    void testSupplierSoftDeleteAndActiveSuppliersExclusion() {
        Supplier s = new Supplier();
        s.setSupplierCode("SUP-DEACT-01");
        s.setSupplierName("Deactivate Test Vendor");
        s.setContactEmail("deact@vendor.lk");
        s.setAddress("Colombo 07, Sri Lanka");
        Supplier created = supplierService.createSupplier(s);
        assertNotNull(created.getId());
        assertTrue(created.getIsActive());
        assertEquals("ACTIVE", created.getStatus());

        try {
            // Deactivate (soft delete)
            Supplier deactivated = supplierService.deactivateSupplier(created.getId());
            assertFalse(deactivated.getIsActive());
            assertEquals("INACTIVE", deactivated.getStatus());

            // Ensure deactivated supplier is excluded from getActiveSuppliers
            List<Supplier> activeList = supplierService.getActiveSuppliers();
            assertFalse(activeList.stream().anyMatch(sup -> sup.getId().equals(created.getId())));

            // Reactivate
            Supplier reactivated = supplierService.activateSupplier(created.getId());
            assertTrue(reactivated.getIsActive());
            assertEquals("ACTIVE", reactivated.getStatus());

            List<Supplier> activeListAfter = supplierService.getActiveSuppliers();
            assertTrue(activeListAfter.stream().anyMatch(sup -> sup.getId().equals(created.getId())));
        } finally {
            supplierService.deleteSupplier(created.getId());
        }
    }

    @Test
    void testSupplierPartsLinkingAndPricingComparison() {
        Supplier s = new Supplier();
        s.setSupplierCode("SUP-PARTS-01");
        s.setSupplierName("Parts Linking Test Vendor");
        s.setContactEmail("partslink@vendor.lk");
        s.setAddress("Colombo 05, Sri Lanka");
        s.setLeadTimeDays(3);
        s.setScore(92);
        Supplier saved = supplierService.createSupplier(s);

        try {
            // Link a hardware part
            com.partshub.supplier.entity.SupplierPart part = new com.partshub.supplier.entity.SupplierPart();
            part.setPartName("Test RTX 4080 Super");
            part.setSkuCode("GPU-RTX-4080S-TEST");
            part.setCategory("GPUs & Power Supplies");
            part.setSupplyPrice(new BigDecimal("345000.00"));
            part.setLeadTimeDays(2);
            part.setAvailabilityStatus("IN_STOCK");

            com.partshub.supplier.entity.SupplierPart linked = supplierService.linkPartToSupplier(saved.getId(), part);
            assertNotNull(linked.getId());
            assertEquals(saved.getId(), linked.getSupplierId());
            assertEquals("Test RTX 4080 Super", linked.getPartName());

            // Verify parts list
            List<com.partshub.supplier.entity.SupplierPart> parts = supplierService.getPartsBySupplier(saved.getId());
            assertEquals(1, parts.size());
            assertEquals("GPU-RTX-4080S-TEST", parts.get(0).getSkuCode());

            // Verify comparison query
            List<com.partshub.supplier.entity.SupplierPart> compList = supplierService.getPricingComparison("GPUs & Power Supplies");
            assertTrue(compList.stream().anyMatch(p -> p.getSkuCode().equals("GPU-RTX-4080S-TEST")));

            // Unlink part
            supplierService.unlinkPartFromSupplier(saved.getId(), linked.getId());
            List<com.partshub.supplier.entity.SupplierPart> afterUnlink = supplierService.getPartsBySupplier(saved.getId());
            assertTrue(afterUnlink.isEmpty());
        } finally {
            supplierService.deleteSupplier(saved.getId());
        }
    }

    @Test
    void testReturnsCrud() {
        com.partshub.returns.entity.ReturnClaim claim = new com.partshub.returns.entity.ReturnClaim();
        claim.setClaimNumber("RMA-TEST-001");
        claim.setClaimType("CUSTOMER_RETURN");
        claim.setPartName("Intel Core i9-14900K Processor");
        claim.setReferenceNumber("INV-2026-1049");
        claim.setPartyName("Lanka Tech Solutions Pvt Ltd");
        claim.setReason("Faulty memory channel");
        claim.setResolution("PENDING");
        claim.setStatus("PENDING");

        var created = returnsService.createClaim(claim);
        assertNotNull(created.getId());

        var fetched = returnsService.getClaimById(created.getId());
        assertTrue(fetched.isPresent());

        created.setReason("Updated Faulty memory channel");
        var updated = returnsService.updateClaim(created.getId(), created);
        assertEquals("Updated Faulty memory channel", updated.getReason());

        returnsService.deleteClaim(created.getId());
        assertTrue(returnsService.getClaimById(created.getId()).isEmpty());
    }

    @Test
    void testDashboardStats() {
        var data = dashboardService.getDashboardData();
        assertNotNull(data);
        assertNotNull(data.getSummaryCards());
        assertNotNull(data.getSalesChart());
        assertNotNull(data.getStockChart());
    }

    @Test
    void testPurchaseOrderAndAutoStockIncrement() {
        // Prepare inventory item
        InventoryItem item = new InventoryItem();
        item.setPartName("Test SSD");
        item.setSkuCode("TEST-SSD-01");
        item.setCategory("Storage");
        item.setSupplierName("Colombo Micro Hub");
        item.setQtyOnHand(10);
        item.setReorderLevel(5);
        item.setUnitCost(new BigDecimal("15000"));
        item.setSellingPrice(new BigDecimal("20000"));
        item.setStatus("IN_STOCK");
        inventoryService.createItem(item);

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-TEST-9001");
        po.setSupplierName("Test Distributor");
        po.setOrderDate(LocalDate.now());
        po.setStatus("SENT");

        PurchaseItem pItem = new PurchaseItem();
        pItem.setPartName("Test SSD");
        pItem.setQtyOrdered(15);
        pItem.setUnitCost(new BigDecimal("15000"));
        pItem.setLineTotal(new BigDecimal("225000"));
        po.addItem(pItem);

        PurchaseOrder savedPo = purchaseService.createOrder(po);

        // Receive PO
        purchaseService.receiveOrder(savedPo.getId(), List.of(Map.of("qtyReceived", 15, "conditionStatus", "GOOD")), "All goods received in good condition");

        InventoryItem after = inventoryService.getItemBySku("TEST-SSD-01").orElseThrow();
        assertEquals(25, after.getQtyOnHand());
    }

    @Test
    void testSaleInvoiceAndAutoStockDecrement() {
        InventoryItem item = new InventoryItem();
        item.setPartName("Test GPU");
        item.setSkuCode("TEST-GPU-01");
        item.setCategory("GPU");
        item.setSupplierName("PC World Suppliers");
        item.setQtyOnHand(30);
        item.setReorderLevel(5);
        item.setUnitCost(new BigDecimal("100000"));
        item.setSellingPrice(new BigDecimal("120000"));
        item.setStatus("IN_STOCK");
        inventoryService.createItem(item);

        SaleInvoice invoice = new SaleInvoice();
        invoice.setInvoiceNumber("INV-TEST-001");
        invoice.setCustomerName("Test Customer");
        invoice.setCustomerContact("+94771234567");
        invoice.setSaleDate(LocalDate.now());
        invoice.setPaymentMethod("Cash");
        invoice.setPaymentStatus("PAID");

        SaleItem sItem = new SaleItem();
        sItem.setPartName("Test GPU");
        sItem.setQuantity(5);
        sItem.setUnitPrice(new BigDecimal("120000"));
        sItem.setLineTotal(new BigDecimal("600000"));
        invoice.addItem(sItem);

        SaleInvoice savedInvoice = salesService.createInvoice(invoice);
        assertNotNull(savedInvoice.getId());

        InventoryItem afterSale = inventoryService.getItemBySku("TEST-GPU-01").orElseThrow();
        assertEquals(25, afterSale.getQtyOnHand());

        // Void invoice - stock restored
        salesService.deleteInvoice(savedInvoice.getId());
        InventoryItem afterVoid = inventoryService.getItemBySku("TEST-GPU-01").orElseThrow();
        assertEquals(30, afterVoid.getQtyOnHand());
    }

    @Autowired
    private com.partshub.inventory.controller.InventoryController inventoryController;

    @Test
    void testInventoryImageUploadAndPersistence() {
        InventoryItem item = new InventoryItem();
        item.setPartName("Test Graphic Card RTX 4070");
        item.setSkuCode("TEST-GPU-4070");
        item.setCategory("GPU");
        item.setSupplierName("PC World Suppliers");
        item.setQtyOnHand(10);
        item.setReorderLevel(3);
        item.setUnitCost(new BigDecimal("150000"));
        item.setSellingPrice(new BigDecimal("180000"));
        item.setWarehouseLocation("Colombo WH-A (Bin 02)");

        org.springframework.mock.web.MockMultipartFile mockFile =
                new org.springframework.mock.web.MockMultipartFile(
                        "image",
                        "test-gpu.png",
                        "image/png",
                        "dummy image data".getBytes()
                );

        var response = (org.springframework.http.ResponseEntity<?>) inventoryController.createItemMultipart(item, mockFile);
        assertEquals(org.springframework.http.HttpStatus.CREATED, response.getStatusCode());
        InventoryItem created = (InventoryItem) response.getBody();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertNotNull(created.getImage());
        assertTrue(created.getImage().startsWith("images/products/"));
        assertTrue(created.getImage().endsWith(".png"));
        assertNotNull(created.getImageFilename());

        // Verify retrieval
        InventoryItem fetched = inventoryService.getItemById(created.getId()).orElseThrow();
        assertEquals(created.getImage(), fetched.getImage());

        // Clean up
        inventoryService.deleteItem(created.getId());
    }
}
