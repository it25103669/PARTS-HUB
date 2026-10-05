package com.partshub.reporting.dto;

import com.partshub.reporting.model.ReportType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Holds the filter criteria submitted by the user on the Reports page.
 * Bound from the HTML form via Spring MVC model binding.
 */
public class ReportFilterDTO {

    private ReportType reportType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private String productName;
    private String category;
    private String supplierName;

    public ReportFilterDTO() {}

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
}
