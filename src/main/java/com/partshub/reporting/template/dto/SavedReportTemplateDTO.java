package com.partshub.reporting.template.dto;

import com.partshub.reporting.model.ChartType;
import com.partshub.reporting.model.DateRangeType;
import com.partshub.reporting.model.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class SavedReportTemplateDTO {

    private Long templateId;

    @NotBlank(message = "Template name is required.")
    @Size(max = 200, message = "Template name cannot exceed 200 characters.")
    private String templateName;

    private String description;

    @NotNull(message = "Report type is required.")
    private ReportType reportType;

    @NotNull(message = "Date range type is required.")
    private DateRangeType dateRangeType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate customStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate customEndDate;

    private String productFilter;
    private String categoryFilter;
    private String supplierFilter;

    @NotNull(message = "Chart type is required.")
    private ChartType chartType;

    public SavedReportTemplateDTO() {
    }

    // Getters and Setters

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
}
