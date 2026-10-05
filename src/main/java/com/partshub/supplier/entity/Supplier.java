package com.partshub.supplier.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier code is required")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "Supplier code can only contain letters, numbers, and hyphens")
    @Size(max = 50, message = "Supplier code cannot exceed 50 characters")
    @Column(name = "supplier_code", nullable = false, unique = true, length = 50)
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(min = 2, max = 150, message = "Supplier name must be between 2 and 150 characters")
    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;

    @Column(length = 100)
    private String category = "General Hardware";

    @Column(length = 50)
    private String tier = "Tier 3 - Silver";

    @Column(name = "orders_completed")
    private Integer ordersCompleted = 0;

    @Column(nullable = false)
    private Integer score = 75;

    @Column(name = "on_time_rate", precision = 5, scale = 2)
    private BigDecimal onTimeRate = new BigDecimal("95.00");

    @Column(name = "defect_rate", precision = 5, scale = 2)
    private BigDecimal defectRate = new BigDecimal("1.00");

    @Column(name = "lead_time_days")
    private Integer leadTimeDays = 5;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Pattern(regexp = "^(\\+?[0-9\\s\\-\\(\\)]{7,25})?$", message = "Invalid contact phone number format")
    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Please enter a valid email address (e.g. procurement@vendor.lk)")
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @NotBlank(message = "Business address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    @Column(name = "address", length = 255)
    private String address = "Colombo, Western Province, Sri Lanka";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Supplier() {}

    public void recalculateTier() {
        if (score >= 90) {
            this.tier = "Tier 1 - Platinum";
        } else if (score >= 80) {
            this.tier = "Tier 2 - Gold";
        } else if (score >= 65) {
            this.tier = "Tier 3 - Silver";
        } else {
            this.tier = "Tier 4 - Bronze";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public Integer getOrdersCompleted() { return ordersCompleted; }
    public void setOrdersCompleted(Integer ordersCompleted) { this.ordersCompleted = ordersCompleted; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { 
        this.score = Math.max(0, Math.min(100, score)); 
        recalculateTier();
    }

    public BigDecimal getOnTimeRate() { return onTimeRate; }
    public void setOnTimeRate(BigDecimal onTimeRate) { this.onTimeRate = onTimeRate; }

    public BigDecimal getDefectRate() { return defectRate; }
    public void setDefectRate(BigDecimal defectRate) { this.defectRate = defectRate; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.isActive = !"INACTIVE".equalsIgnoreCase(status);
    }

    public Boolean getIsActive() { 
        if (isActive == null) {
            return !"INACTIVE".equalsIgnoreCase(status);
        }
        return isActive; 
    }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive;
        this.status = (isActive != null && isActive) ? "ACTIVE" : "INACTIVE";
    }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
