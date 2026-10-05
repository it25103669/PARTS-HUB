package com.partshub.returns.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_claims")
public class ReturnClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 50)
    private String claimNumber;

    @Column(name = "claim_type", nullable = false, length = 30)
    private String claimType; // CUSTOMER_RETURN, SUPPLIER_RETURN, WARRANTY_CLAIM

    @Column(name = "reference_number", nullable = false, length = 50)
    private String referenceNumber; // INV-XXXX or PO-XXXX

    @Column(name = "part_name", nullable = false, length = 200)
    private String partName;

    @Column(name = "party_name", length = 150)
    private String partyName;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "item_condition", length = 50)
    private String itemCondition = "DEFECTIVE"; // SEALED, OPEN_BOX, DEFECTIVE, DAMAGED

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(nullable = false, length = 100)
    private String resolution; // Refund, Replacement, Store Credit, Repair, Reject - Customer Misuse

    @Column(name = "diagnostic_notes", columnDefinition = "TEXT")
    private String diagnosticNotes;

    @Column(name = "sale_date")
    private LocalDate saleDate;

    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths = 12;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "in_warranty")
    private Boolean inWarranty = true;

    @Column(name = "goodwill_override")
    private Boolean goodwillOverride = false;

    @Column(name = "goodwill_reason", length = 255)
    private String goodwillReason;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    @Column(name = "supplier_claim_status", length = 50)
    private String supplierClaimStatus = "NOT_APPLICABLE"; // NOT_APPLICABLE, PENDING_SUPPLIER, SUPPLIER_APPROVED, SUPPLIER_REJECTED

    @Column(length = 50)
    private String status = "PENDING"; // PENDING, APPROVED, RESOLVED, REJECTED, PENDING_SUPPLIER_CLAIM

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReturnClaim() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public Integer getQuantity() { return quantity != null ? quantity : 1; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getItemCondition() { return itemCondition; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getDiagnosticNotes() { return diagnosticNotes; }
    public void setDiagnosticNotes(String diagnosticNotes) { this.diagnosticNotes = diagnosticNotes; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public Integer getWarrantyPeriodMonths() { return warrantyPeriodMonths; }
    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) { this.warrantyPeriodMonths = warrantyPeriodMonths; }

    public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }

    public Boolean getInWarranty() { return inWarranty; }
    public void setInWarranty(Boolean inWarranty) { this.inWarranty = inWarranty; }

    public Boolean getGoodwillOverride() { return goodwillOverride; }
    public void setGoodwillOverride(Boolean goodwillOverride) { this.goodwillOverride = goodwillOverride; }

    public String getGoodwillReason() { return goodwillReason; }
    public void setGoodwillReason(String goodwillReason) { this.goodwillReason = goodwillReason; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierClaimStatus() { return supplierClaimStatus; }
    public void setSupplierClaimStatus(String supplierClaimStatus) { this.supplierClaimStatus = supplierClaimStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
