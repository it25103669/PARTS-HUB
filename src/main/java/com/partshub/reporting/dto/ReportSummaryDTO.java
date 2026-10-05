package com.partshub.reporting.dto;

import java.time.LocalDateTime;

/**
 * High-level metadata about a generated report shown above the data table.
 */
public class ReportSummaryDTO {

    private String reportTitle;
    private LocalDateTime generatedAt;
    private int totalRows;
    private String filterDescription; // Human-readable summary of applied filters

    public ReportSummaryDTO() {}

    public ReportSummaryDTO(String reportTitle, LocalDateTime generatedAt,
                            int totalRows, String filterDescription) {
        this.reportTitle = reportTitle;
        this.generatedAt = generatedAt;
        this.totalRows = totalRows;
        this.filterDescription = filterDescription;
    }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public String getFilterDescription() { return filterDescription; }
    public void setFilterDescription(String filterDescription) {
        this.filterDescription = filterDescription;
    }
}
