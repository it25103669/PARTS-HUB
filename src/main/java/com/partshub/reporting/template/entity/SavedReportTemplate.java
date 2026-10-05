package com.partshub.reporting.template.entity;

import com.partshub.reporting.model.ChartType;
import com.partshub.reporting.model.DateRangeType;
import com.partshub.reporting.model.ReportType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a saved report configuration template.
 */
@Entity
@Table(name = "saved_report_template")
public class SavedReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name", nullable = false, length = 200, unique = true)
    private String templateName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_range_type", nullable = false)
    private DateRangeType dateRangeType;

    @Column(name = "custom_start_date")
    private LocalDate customStartDate;

    @Column(name = "custom_end_date")
    private LocalDate customEndDate;

    @Column(name = "product_filter")
    private String productFilter;

    @Column(name = "category_filter")
    private String categoryFilter;

    @Column(name = "supplier_filter")
    private String supplierFilter;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false)
    private ChartType chartType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SavedReportTemplate() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters (No Lombok)

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public DateRangeType getDateRangeType() {
        return dateRangeType;
    }

    public void setDateRangeType(DateRangeType dateRangeType) {
        this.dateRangeType = dateRangeType;
    }

    public LocalDate getCustomStartDate() {
        return customStartDate;
    }

    public void setCustomStartDate(LocalDate customStartDate) {
        this.customStartDate = customStartDate;
    }

    public LocalDate getCustomEndDate() {
        return customEndDate;
    }

    public void setCustomEndDate(LocalDate customEndDate) {
        this.customEndDate = customEndDate;
    }

    public String getProductFilter() {
        return productFilter;
    }

    public void setProductFilter(String productFilter) {
        this.productFilter = productFilter;
    }

    public String getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public String getSupplierFilter() {
        return supplierFilter;
    }

    public void setSupplierFilter(String supplierFilter) {
        this.supplierFilter = supplierFilter;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
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
