package com.partshub.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Part name is required")
    @Size(max = 200, message = "Part name cannot exceed 200 characters")
    @Column(name = "part_name", nullable = false, length = 200)
    private String partName;

    @Column(name = "sku_code", nullable = false, unique = true, length = 100)
    private String skuCode;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @NotBlank(message = "Brand is required")
    @Column(length = 100)
    private String brand = "OEM / Generic";

    @Column(length = 200)
    private String compatibility = "Universal PC Standard";

    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName = "Tech Lanka Pvt Ltd";

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    @Column(name = "qty_on_hand", nullable = false)
    private Integer qtyOnHand = 0;

    @NotNull(message = "Reorder threshold is required")
    @PositiveOrZero(message = "Reorder threshold cannot be negative")
    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 10;

    @NotNull(message = "Unit cost is required")
    @Positive(message = "Unit cost must be greater than 0")
    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be greater than 0")
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(name = "warehouse_location", nullable = false, length = 100)
    private String warehouseLocation = "Colombo WH-A (Bin 01)";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image", length = 255)
    private String image;

    @Column(length = 20)
    private String status = "IN_STOCK";

    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths = 12;

    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus = "PENDING";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InventoryItem() {}

    public String getBrand() { return (brand == null || brand.isBlank()) ? "OEM / Generic" : brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCompatibility() { return compatibility; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }

    // Alias for minimumStockThreshold required by prompt
    public Integer getMinimumStockThreshold() { return reorderLevel; }
    public void setMinimumStockThreshold(Integer threshold) { this.reorderLevel = threshold; }

    @PrePersist
    @PreUpdate
    public void calculateStatus() {
        if (this.brand == null || this.brand.isBlank()) {
            this.brand = "OEM / Generic";
        }
        if (this.supplierName == null || this.supplierName.isBlank()) {
            this.supplierName = "Tech Lanka Pvt Ltd";
        }
        if (this.warehouseLocation == null || this.warehouseLocation.isBlank()) {
            this.warehouseLocation = "Colombo WH-A (Bin 01)";
        }
        this.updatedAt = LocalDateTime.now();
        if (qtyOnHand == null || qtyOnHand <= 0) {
            this.status = "OUT_OF_STOCK";
        } else if (reorderLevel != null && qtyOnHand <= reorderLevel) {
            this.status = "LOW_STOCK";
        } else {
            this.status = "IN_STOCK";
        }
    }

    public BigDecimal getTotalValue() {
        if (qtyOnHand == null || unitCost == null) return BigDecimal.ZERO;
        return unitCost.multiply(BigDecimal.valueOf(qtyOnHand));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public Integer getQtyOnHand() { return qtyOnHand; }
    public void setQtyOnHand(Integer qtyOnHand) { this.qtyOnHand = qtyOnHand; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getImageFilename() {
        if (image == null || image.isBlank()) return null;
        if (image.contains("/")) {
            return image.substring(image.lastIndexOf('/') + 1);
        }
        return image;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getWarrantyPeriodMonths() { return warrantyPeriodMonths != null ? warrantyPeriodMonths : 12; }
    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) { this.warrantyPeriodMonths = warrantyPeriodMonths; }

    public String getApprovalStatus() { return approvalStatus != null ? approvalStatus : "APPROVED"; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
