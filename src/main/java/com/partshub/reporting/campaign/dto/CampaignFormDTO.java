package com.partshub.reporting.campaign.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Form-backing DTO used for both the Create and Edit Promotional Campaign forms.
 *
 * <p>Bean Validation annotations are applied here so that the controller
 * can use {@code @Valid} to trigger server-side validation automatically.
 * All validation messages are user-friendly and suitable for display next
 * to the relevant form field via Thymeleaf {@code th:errors}.
 */
public class CampaignFormDTO {

    // campaignId is null for creates, populated for edits
    private Long campaignId;

    @NotBlank(message = "Campaign name is required.")
    @Size(max = 200, message = "Campaign name cannot exceed 200 characters.")
    private String campaignName;

    // description is optional
    private String description;

    @NotBlank(message = "Please select or enter at least one product.")
    private String selectedProducts;

    @NotNull(message = "Discount percentage is required.")
    @DecimalMin(value = "0.01", message = "Discount percentage must be greater than 0.")
    @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100.")
    private BigDecimal discountPercentage;

    @NotNull(message = "Start date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "End date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    // ── Constructors ───────────────────────────────────────────────────────

    public CampaignFormDTO() {}

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
}
