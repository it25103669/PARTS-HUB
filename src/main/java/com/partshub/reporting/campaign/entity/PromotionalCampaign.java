package com.partshub.reporting.campaign.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a Promotional Campaign.
 *
 * <p>Maps to the {@code promotional_campaign} table in the PartsHub database.
 * Hibernate creates / updates the table automatically via {@code ddl-auto=update}.
 *
 * <p>The {@code selectedProducts} field is a plain String for now.
 * TODO: In a future integration phase, replace {@code selectedProducts}
 *       with a proper ManyToMany relationship to the Inventory Product/SparePart entity
 *       once that module is stable (see com.partshub.inventory).
 *
 * <p>Status is NOT set by the user; it is resolved automatically by
 * {@link com.partshub.reporting.campaign.service.PromotionalCampaignServiceImpl#resolveStatus}.
 * The only exception is CANCELLED, which is set explicitly by the cancel action
 * and must remain CANCELLED even if dates would otherwise make it ACTIVE.
 */
@Entity
@Table(name = "promotional_campaign")
public class PromotionalCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "campaign_name", nullable = false, length = 200)
    private String campaignName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Comma-separated list of product names included in this campaign.
     * Example: "NVIDIA RTX 4060, Kingston 16GB RAM, Samsung 1TB SSD"
     *
     * TODO: Replace with a proper join table / relationship to Inventory Product entity
     *       once that module is implemented by the Inventory team member.
     */
    @Column(name = "selected_products", nullable = false, columnDefinition = "TEXT")
    private String selectedProducts;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampaignStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Lifecycle callbacks ────────────────────────────────────────────────

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Constructors ───────────────────────────────────────────────────────

    public PromotionalCampaign() {}

    // ── Getters and Setters ────────────────────────────────────────────────

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(String selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
