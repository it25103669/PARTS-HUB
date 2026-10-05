package com.partshub.reporting.service;

import com.partshub.reporting.dto.ReportFilterDTO;
import com.partshub.reporting.dto.ReportRowDTO;
import com.partshub.reporting.dto.ReportSummaryDTO;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Contract for report generation, filtering, and export.
 * <p>
 * Current implementation: {@link MockReportDataProvider}
 * Future implementation: DatabaseReportDataProvider
 */
public interface ReportService {

    /**
     * Returns metadata about the report (title, row count, applied filters).
     */
    ReportSummaryDTO getReportSummary(ReportFilterDTO filter);

    /**
     * Returns the column header names for the given report type.
     */
    List<String> getColumnHeaders(ReportFilterDTO filter);

    /**
     * Returns the data rows for the given filter criteria.
     */
    List<ReportRowDTO> generateReport(ReportFilterDTO filter);

    /**
     * Exports the report as a PDF file written to the HTTP response.
     * TODO: Implement using iText or JasperReports in a future phase.
     */
    void exportPdf(ReportFilterDTO filter, HttpServletResponse response);

    /**
     * Exports the report as an Excel file written to the HTTP response.
     * TODO: Implement using Apache POI in a future phase.
     */
    void exportExcel(ReportFilterDTO filter, HttpServletResponse response);
}
