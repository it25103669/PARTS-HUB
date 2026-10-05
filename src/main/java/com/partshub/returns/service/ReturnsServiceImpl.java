package com.partshub.returns.service;

import com.partshub.inventory.entity.InventoryItem;
import com.partshub.inventory.repository.InventoryItemRepository;
import com.partshub.inventory.service.InventoryService;
import com.partshub.returns.dto.SaleItemLookupDTO;
import com.partshub.returns.dto.SaleLookupDTO;
import com.partshub.returns.entity.ReturnClaim;
import com.partshub.returns.repository.ReturnClaimRepository;
import com.partshub.sales.entity.SaleInvoice;
import com.partshub.sales.entity.SaleItem;
import com.partshub.sales.repository.SaleInvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class ReturnsServiceImpl implements ReturnsService {

    private final ReturnClaimRepository repository;
    private final SaleInvoiceRepository invoiceRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryService inventoryService;

    public ReturnsServiceImpl(ReturnClaimRepository repository,
                              SaleInvoiceRepository invoiceRepository,
                              InventoryItemRepository inventoryItemRepository,
                              InventoryService inventoryService) {
        this.repository = repository;
        this.invoiceRepository = invoiceRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnClaim> getAllClaims() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnClaim> searchAndFilter(String query, String type, String status) {
        return repository.searchAndFilter(query, type, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnClaim> getClaimById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnClaim> getClaimByNumber(String claimNumber) {
        return repository.findByClaimNumber(claimNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleLookupDTO lookupSale(String invoiceNumber, String customerName) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Invoice number is required for sale lookup");
        }

        SaleInvoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Sale Record: Invoice " + invoiceNumber + " not found."));

        if (customerName != null && !customerName.trim().isEmpty()) {
            if (!invoice.getCustomerName().equalsIgnoreCase(customerName.trim()) &&
                !invoice.getCustomerName().toLowerCase().contains(customerName.trim().toLowerCase())) {
                throw new IllegalArgumentException("Invalid Customer Details: Provided customer name '" + customerName + 
                        "' does not match sale invoice " + invoiceNumber + " (registered customer: " + invoice.getCustomerName() + ").");
            }
        }

        SaleLookupDTO dto = new SaleLookupDTO();
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerName(invoice.getCustomerName());
        dto.setCustomerContact(invoice.getCustomerContact());
        dto.setSaleDate(invoice.getSaleDate());

        for (SaleItem item : invoice.getItems()) {
            SaleItemLookupDTO itemDto = new SaleItemLookupDTO();
            itemDto.setPartName(item.getPartName());
            itemDto.setSkuCode(item.getSkuCode());
            itemDto.setOriginalQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());

            int returnedQty = repository.countReturnedQtyByRefAndPart(invoice.getInvoiceNumber(), item.getPartName());
            itemDto.setReturnedQuantity(returnedQty);
            int returnableQty = Math.max(0, item.getQuantity() - returnedQty);
            itemDto.setReturnableQuantity(returnableQty);

            Optional<InventoryItem> invOpt = Optional.empty();
            if (item.getSkuCode() != null && !item.getSkuCode().isBlank()) {
                invOpt = inventoryItemRepository.findBySkuCode(item.getSkuCode());
            }
            if (invOpt.isEmpty()) {
                List<InventoryItem> invList = inventoryItemRepository.findByPartNameContainingIgnoreCase(item.getPartName());
                if (!invList.isEmpty()) invOpt = Optional.of(invList.get(0));
            }

            int warrantyMonths = invOpt.map(InventoryItem::getWarrantyPeriodMonths).orElse(12);
            LocalDate saleDate = invoice.getSaleDate() != null ? invoice.getSaleDate() : LocalDate.now();
            LocalDate expiryDate = saleDate.plusMonths(warrantyMonths);
            boolean inWarranty = !LocalDate.now().isAfter(expiryDate);

            itemDto.setWarrantyPeriodMonths(warrantyMonths);
            itemDto.setWarrantyExpiryDate(expiryDate);
            itemDto.setInWarranty(inWarranty);

            dto.getItems().add(itemDto);
        }

        return dto;
    }

    @Override
    @Transactional
    public ReturnClaim createClaim(ReturnClaim claim) {
        if (claim.getClaimNumber() == null || claim.getClaimNumber().trim().isEmpty()) {
            claim.setClaimNumber("CLM-2026-" + String.format("%04d", 400 + new Random().nextInt(600)));
        }

        String claimType = claim.getClaimType() != null ? claim.getClaimType() : "CUSTOMER_RETURN";
        String refNum = claim.getReferenceNumber() != null ? claim.getReferenceNumber().trim() : "";
        String partName = claim.getPartName() != null ? claim.getPartName().trim() : "";
        int requestedQty = claim.getQuantity() != null ? claim.getQuantity() : 1;

        // Step 3 & 4: Validate Sale Record & Customer if CUSTOMER_RETURN or WARRANTY_CLAIM
        if ("CUSTOMER_RETURN".equalsIgnoreCase(claimType) || "WARRANTY_CLAIM".equalsIgnoreCase(claimType)) {
            SaleInvoice invoice = invoiceRepository.findByInvoiceNumber(refNum)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Sale Record: Invoice " + refNum + " not found."));

            if (claim.getPartyName() != null && !claim.getPartyName().trim().isEmpty()) {
                if (!invoice.getCustomerName().equalsIgnoreCase(claim.getPartyName().trim()) &&
                    !invoice.getCustomerName().toLowerCase().contains(claim.getPartyName().trim().toLowerCase())) {
                    throw new IllegalArgumentException("Invalid Customer Details: Provided customer name '" + claim.getPartyName() + 
                            "' does not match sale invoice " + refNum + " (registered customer: " + invoice.getCustomerName() + ").");
                }
            } else {
                claim.setPartyName(invoice.getCustomerName());
            }

            claim.setSaleDate(invoice.getSaleDate());

            // Check part on sale item & returnable quantity
            SaleItem matchingItem = invoice.getItems().stream()
                    .filter(i -> i.getPartName().equalsIgnoreCase(partName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Item '" + partName + "' was not found on sale invoice " + refNum + "."));

            int returnedQty = repository.countReturnedQtyByRefAndPart(refNum, partName);
            int returnableQty = Math.max(0, matchingItem.getQuantity() - returnedQty);

            if (requestedQty > returnableQty) {
                throw new IllegalArgumentException("Return Quantity Exceeded: Requested " + requestedQty + 
                        " unit(s), but only " + returnableQty + " remain returnable for '" + partName + "' on invoice " + refNum + ".");
            }
        }

        // Step 5: Warranty Eligibility
        Optional<InventoryItem> invOpt = Optional.empty();
        List<InventoryItem> items = inventoryItemRepository.findByPartNameContainingIgnoreCase(partName);
        if (!items.isEmpty()) invOpt = Optional.of(items.get(0));

        int warrantyMonths = invOpt.map(InventoryItem::getWarrantyPeriodMonths).orElse(12);
        LocalDate saleDate = claim.getSaleDate() != null ? claim.getSaleDate() : LocalDate.now();
        LocalDate expiryDate = saleDate.plusMonths(warrantyMonths);
        boolean inWarranty = !LocalDate.now().isAfter(expiryDate);

        claim.setWarrantyPeriodMonths(warrantyMonths);
        claim.setWarrantyExpiryDate(expiryDate);
        claim.setInWarranty(inWarranty);

        if (claim.getSupplierName() == null || claim.getSupplierName().isBlank()) {
            invOpt.ifPresent(i -> claim.setSupplierName(i.getSupplierName()));
        }

        // Step 7: Customer Misuse Rejection
        String resolution = claim.getResolution() != null ? claim.getResolution() : "Pending Review";
        String reason = claim.getReason() != null ? claim.getReason() : "";

        if ("CUSTOMER_MISUSE".equalsIgnoreCase(reason) || 
            resolution.toLowerCase().contains("customer misuse") ||
            "REJECTED".equalsIgnoreCase(claim.getStatus())) {
            claim.setStatus("REJECTED");
            claim.setResolution("Rejected — Customer Misuse");
            if (claim.getRejectionReason() == null || claim.getRejectionReason().isBlank()) {
                claim.setRejectionReason(claim.getDiagnosticNotes() != null ? claim.getDiagnosticNotes() : "Customer misuse / physical damage.");
            }
            return repository.save(claim);
        }

        // Step 6: Goodwill Override tracking if out of warranty
        if (!inWarranty && !"REJECTED".equalsIgnoreCase(claim.getStatus())) {
            Boolean goodwill = claim.getGoodwillOverride();
            if (goodwill == null || !goodwill || claim.getGoodwillReason() == null || claim.getGoodwillReason().isBlank()) {
                throw new IllegalArgumentException("Warranty Expired: Item purchase date (" + saleDate + 
                        ") expired on " + expiryDate + ". A goodwill override and reason are required to process this claim.");
            }
        }

        // Step 9: Supplier Routing for Manufacturing Defects
        if ("MANUFACTURING_DEFECT".equalsIgnoreCase(reason) || 
            "SUPPLIER_RETURN".equalsIgnoreCase(claimType) ||
            reason.toLowerCase().contains("manufacturing defect")) {
            claim.setStatus("PENDING_SUPPLIER_CLAIM");
            claim.setSupplierClaimStatus("PENDING_SUPPLIER");
            if (claim.getSupplierName() == null || claim.getSupplierName().isBlank()) {
                claim.setSupplierName("Nanotech Hardware");
            }
            // Hold inventory stock update until supplier claim is resolved!
            return repository.save(claim);
        }

        // Step 8: Normal Resolution Processing & Inventory Update
        if (claim.getStatus() == null || claim.getStatus().isBlank()) {
            claim.setStatus("PENDING");
        }

        ReturnClaim saved = repository.save(claim);
        applyInventoryResolution(saved, invOpt);
        return saved;
    }

    @Override
    @Transactional
    public ReturnClaim updateClaim(Long id, ReturnClaim details) {
        ReturnClaim existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Return claim not found with id: " + id));

        String currentStatus = existing.getStatus() != null ? existing.getStatus().toUpperCase() : "";
        if ("RESOLVED".equals(currentStatus) || "REJECTED".equals(currentStatus) || "APPROVED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot update claim in finalized status '" + existing.getStatus() + "'.");
        }

        existing.setClaimType(details.getClaimType());
        existing.setReferenceNumber(details.getReferenceNumber());
        existing.setPartName(details.getPartName());
        existing.setPartyName(details.getPartyName());
        existing.setQuantity(details.getQuantity());
        existing.setItemCondition(details.getItemCondition());
        existing.setReason(details.getReason());
        existing.setResolution(details.getResolution());
        existing.setDiagnosticNotes(details.getDiagnosticNotes());
        if (details.getStatus() != null && !details.getStatus().isBlank()) {
            existing.setStatus(details.getStatus());
        }
        if (details.getRejectionReason() != null) {
            existing.setRejectionReason(details.getRejectionReason());
        }
        if (details.getGoodwillOverride() != null) {
            existing.setGoodwillOverride(details.getGoodwillOverride());
        }
        if (details.getGoodwillReason() != null) {
            existing.setGoodwillReason(details.getGoodwillReason());
        }

        return repository.save(existing);
    }

    @Override
    @Transactional
    public ReturnClaim updateStatus(Long id, String status, String resolution) {
        ReturnClaim existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Return claim not found with id: " + id));

        String oldStatus = existing.getStatus();
        existing.setStatus(status);
        if (resolution != null && !resolution.trim().isEmpty()) {
            existing.setResolution(resolution);
        }

        ReturnClaim saved = repository.save(existing);
        if ("APPROVED".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status)) {
            if (!"APPROVED".equalsIgnoreCase(oldStatus) && !"RESOLVED".equalsIgnoreCase(oldStatus)) {
                applyInventoryResolution(saved, Optional.empty());
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public ReturnClaim resolveSupplierClaim(Long id, String supplierResolution, String notes) {
        ReturnClaim existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Return claim not found with id: " + id));

        existing.setStatus("RESOLVED");
        existing.setSupplierClaimStatus("SUPPLIER_APPROVED");
        if (supplierResolution != null && !supplierResolution.isBlank()) {
            existing.setResolution(supplierResolution);
        }
        if (notes != null && !notes.isBlank()) {
            existing.setDiagnosticNotes((existing.getDiagnosticNotes() != null ? existing.getDiagnosticNotes() + "\n" : "") + "Supplier Resolution: " + notes);
        }

        ReturnClaim saved = repository.save(existing);
        applyInventoryResolution(saved, Optional.empty());
        return saved;
    }

    private void applyInventoryResolution(ReturnClaim claim, Optional<InventoryItem> invOpt) {
        if ("REJECTED".equalsIgnoreCase(claim.getStatus()) || "PENDING_SUPPLIER_CLAIM".equalsIgnoreCase(claim.getStatus()) || "PENDING".equalsIgnoreCase(claim.getStatus())) {
            return;
        }

        if (invOpt.isEmpty()) {
            List<InventoryItem> items = inventoryItemRepository.findByPartNameContainingIgnoreCase(claim.getPartName());
            if (!items.isEmpty()) invOpt = Optional.of(items.get(0));
        }

        if (invOpt.isPresent()) {
            Long partId = invOpt.get().getId();
            String resolution = claim.getResolution() != null ? claim.getResolution() : "";
            String condition = claim.getItemCondition() != null ? claim.getItemCondition() : "DEFECTIVE";
            int qty = claim.getQuantity() != null ? claim.getQuantity() : 1;

            if ("Refund".equalsIgnoreCase(resolution) || "Store Credit".equalsIgnoreCase(resolution)) {
                if ("SEALED".equalsIgnoreCase(condition) || "RESELLABLE".equalsIgnoreCase(condition) || "OPEN_BOX".equalsIgnoreCase(condition)) {
                    // Resellable condition -> increment stock back into inventory
                    inventoryService.updateStock(partId, qty);
                }
            } else if ("Replace".equalsIgnoreCase(resolution) || "Replacement".equalsIgnoreCase(resolution)) {
                // Replacement unit issued to customer -> decrement stock by qty
                inventoryService.updateStock(partId, -qty);
            }
        }
    }

    @Override
    @Transactional
    public void deleteClaim(Long id) {
        ReturnClaim existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Return claim not found with id: " + id));

        String currentStatus = existing.getStatus() != null ? existing.getStatus().toUpperCase() : "";
        if (!"PENDING".equals(currentStatus)) {
            throw new IllegalStateException("Cannot delete claim in status '" + existing.getStatus() + "'. Only pending claims with no processed side effects can be deleted.");
        }

        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return repository.count();
    }
}
