package com.partshub.supplier.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_parts")
public class SupplierPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Supplier ID is required")
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    @NotBlank(message = "Part name is required")
    @Column(name = "part_name", nullable = false, length = 200)
    private String partName;

    @NotBlank(message = "SKU code is required")
    @Column(name = "sku_code", nullable = false, length = 100)
    private String skuCode;

    @Column(length = 100)
    private String category;

    @NotNull(message = "Supply price is required")
    @DecimalMin(value = "0.01", message = "Supply price must be greater than zero")
    @Column(name = "supply_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal supplyPrice;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays = 5;

    @Column(name = "availability_status", length = 30)
    private String availabilityStatus = "AVAILABLE";

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupplierPart() {}

    public SupplierPart(Long supplierId, String supplierName, String partName, String skuCode, String category, BigDecimal supplyPrice, Integer leadTimeDays) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.partName = partName;
        this.skuCode = skuCode;
        this.category = category;
        this.supplyPrice = supplyPrice;
        this.leadTimeDays = leadTimeDays != null ? leadTimeDays : 5;
        this.availabilityStatus = "AVAILABLE";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getSupplyPrice() { return supplyPrice; }
    public void setSupplyPrice(BigDecimal supplyPrice) { this.supplyPrice = supplyPrice; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
