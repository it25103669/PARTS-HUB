package com.partshub.reporting.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.partshub.reporting.dto.ReportFilterDTO;
import com.partshub.reporting.dto.ReportRowDTO;
import com.partshub.reporting.dto.ReportSummaryDTO;
import com.partshub.reporting.model.ReportType;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Report Data Provider & Export Service
 * Supports PDF (OpenPDF) and Excel (Apache POI) document generation.
 */
@Service
public class MockReportDataProvider implements ReportService {

    @Override
    public ReportSummaryDTO getReportSummary(ReportFilterDTO filter) {
        ReportType type = filter.getReportType() != null ? filter.getReportType() : ReportType.INVENTORY;
        List<ReportRowDTO> rows = generateReport(filter);
        String filterDesc = buildFilterDescription(filter);
        return new ReportSummaryDTO(type.getDisplayName(), LocalDateTime.now(), rows.size(), filterDesc);
    }

    @Override
    public List<String> getColumnHeaders(ReportFilterDTO filter) {
        if (filter.getReportType() == null) return Arrays.asList("Select a report type");
        return switch (filter.getReportType()) {
            case INVENTORY          -> Arrays.asList("Product", "Category", "Current Qty", "Reorder Level", "Stock Status");
            case SALES              -> Arrays.asList("Date", "Invoice #", "Product", "Qty", "Unit Price", "Total");
            case PURCHASE           -> Arrays.asList("PO Number", "Supplier", "Date", "Status", "Amount");
            case LOW_STOCK          -> Arrays.asList("Product", "Category", "Current Qty", "Reorder Level", "Urgency");
            case SUPPLIER_PERFORMANCE -> Arrays.asList("Supplier", "Total Orders", "Completed", "Avg Delivery Days", "Reliability");
            case PRODUCT_PERFORMANCE  -> Arrays.asList("Product", "Qty Sold", "Revenue", "Return Count", "Performance");
            case GROSS_PROFIT         -> Arrays.asList("Product / Period", "Sales Revenue", "Purchase Cost", "Est. Gross Profit");
        };
    }

    @Override
    public List<ReportRowDTO> generateReport(ReportFilterDTO filter) {
        if (filter.getReportType() == null) return buildInventoryRows();
        return switch (filter.getReportType()) {
            case INVENTORY          -> buildInventoryRows();
            case SALES              -> buildSalesRows();
            case PURCHASE           -> buildPurchaseRows();
            case LOW_STOCK          -> buildLowStockRows();
            case SUPPLIER_PERFORMANCE -> buildSupplierPerformanceRows();
            case PRODUCT_PERFORMANCE  -> buildProductPerformanceRows();
            case GROSS_PROFIT         -> buildGrossProfitRows();
        };
    }

    // ── Real PDF Export via OpenPDF ──────────────────────────────────
    @Override
    public void exportPdf(ReportFilterDTO filter, HttpServletResponse response) {
        ReportSummaryDTO summary = getReportSummary(filter);
        List<String> headers = getColumnHeaders(filter);
        List<ReportRowDTO> rows = generateReport(filter);
        String reportTypeName = filter.getReportType() != null ? filter.getReportType().name().toLowerCase() : "telemetry";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"partshub_" + reportTypeName + "_report.pdf\"");

        try {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
            Paragraph title = new Paragraph("PARTSHUB ENTERPRISE — " + summary.getReportTitle().toUpperCase() + " REPORT", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4);
            document.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            String dateStr = summary.getGeneratedAt() != null ? summary.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
            Paragraph sub = new Paragraph("Generated At: " + dateStr + " | Filters: " + summary.getFilterDescription(), subFont);
            sub.setSpacingAfter(16);
            document.add(sub);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Color headerBg = new Color(30, 41, 59);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(7);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Color.BLACK);
            for (ReportRowDTO row : rows) {
                for (String colValue : row.getValues()) {
                    PdfPCell cell = new PdfPCell(new Phrase(colValue != null ? colValue : "", dataFont));
                    cell.setPadding(6);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }

    // ── Real Excel Export via Apache POI ──────────────────────────────
    @Override
    public void exportExcel(ReportFilterDTO filter, HttpServletResponse response) {
        ReportSummaryDTO summary = getReportSummary(filter);
        List<String> headers = getColumnHeaders(filter);
        List<ReportRowDTO> rows = generateReport(filter);
        String reportTypeName = filter.getReportType() != null ? filter.getReportType().name().toLowerCase() : "telemetry";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"partshub_" + reportTypeName + "_report.xlsx\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report Data");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());

            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title Row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PartsHub Enterprise — " + summary.getReportTitle() + " Report");
            
            // Subtitle Row
            Row subRow = sheet.createRow(1);
            Cell subCell = subRow.createCell(0);
            String dateStr = summary.getGeneratedAt() != null ? summary.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
            subCell.setCellValue("Generated: " + dateStr + " | " + summary.getFilterDescription());

            // Column Header Row
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIndex = 4;
            for (ReportRowDTO rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                List<String> values = rowData.getValues();
                for (int c = 0; c < values.size(); c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(values.get(c) != null ? values.get(c) : "");
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage(), e);
        }
    }

    // ── Row Builders ──────────────────────────────────────────

    private List<ReportRowDTO> buildInventoryRows() {
        return Arrays.asList(
            row("Intel Core i9-14900K Processor", "CPU",          "48", "10", "In Stock"),
            row("ASUS ROG Strix RTX 4080 OC",     "GPU",          "4",  "8",  "Low Stock"),
            row("Corsair Vengeance DDR5 32GB",    "RAM",          "112","25", "In Stock"),
            row("MSI MAG Z790 Tomahawk WiFi",     "Motherboard",  "26", "12", "In Stock"),
            row("Samsung 990 PRO 2TB NVMe SSD",   "Storage",      "84", "20", "In Stock"),
            row("Corsair RM850x 850W Gold PSU",   "PSU",          "0",  "15", "Out of Stock"),
            row("DeepCool LT720 360mm AIO Cooler","Cooling",      "35", "10", "In Stock")
        );
    }

    private List<ReportRowDTO> buildSalesRows() {
        return Arrays.asList(
            row("2026-03-01", "INV-2026-0891", "Intel Core i9-14900K Processor", "2", "LKR 110,000", "LKR 220,000"),
            row("2026-03-02", "INV-2026-0890", "ASUS ROG Strix RTX 4080 OC",    "1", "LKR 370,000", "LKR 370,000"),
            row("2026-03-03", "INV-2026-0889", "Samsung 990 PRO 2TB NVMe SSD",  "3", "LKR 28,000",  "LKR 84,000"),
            row("2026-03-04", "INV-2026-0888", "Corsair Vengeance DDR5 32GB",   "2", "LKR 32,000",  "LKR 64,000"),
            row("2026-03-05", "INV-2026-0887", "MSI MAG Z790 Tomahawk WiFi",    "1", "LKR 90,000",  "LKR 90,000")
        );
    }

    private List<ReportRowDTO> buildPurchaseRows() {
        return Arrays.asList(
            row("PO-2026-101", "Tech Lanka Pvt Ltd", "2026-02-15", "RECEIVED", "LKR 2,750,000"),
            row("PO-2026-102", "PC World Suppliers", "2026-02-18", "SENT",     "LKR 1,480,000"),
            row("PO-2026-103", "Colombo Micro Hub",  "2026-02-20", "DRAFT",    "LKR 850,000")
        );
    }

    private List<ReportRowDTO> buildLowStockRows() {
        return Arrays.asList(
            row("ASUS ROG Strix RTX 4080 OC",  "GPU",  "4", "8",  "HIGH URGENCY"),
            row("Logitech G Pro X Superlight", "Peripherals", "5", "12", "MEDIUM URGENCY"),
            row("Corsair RM850x 850W Gold PSU","PSU",  "0", "15", "CRITICAL - OUT OF STOCK")
        );
    }

    private List<ReportRowDTO> buildSupplierPerformanceRows() {
        return Arrays.asList(
            row("Tech Lanka Pvt Ltd",    "45", "44", "2.1 Days", "98.5% Excellent"),
            row("PC World Suppliers",    "32", "30", "3.4 Days", "94.2% Good"),
            row("Colombo Micro Hub",     "28", "28", "1.8 Days", "99.0% Top Tier"),
            row("Nanotech Hardware",     "18", "16", "4.1 Days", "88.9% Satisfactory")
        );
    }

    private List<ReportRowDTO> buildProductPerformanceRows() {
        return Arrays.asList(
            row("Intel Core i9-14900K",    "142", "LKR 15,620,000", "3", "Top Seller"),
            row("NVIDIA RTX 4080 OC",      "118", "LKR 43,660,000", "5", "Top Seller"),
            row("Samsung 990 PRO 2TB SSD", "97",  "LKR 2,716,000",  "2", "High Turn-Over"),
            row("Corsair DDR5 32GB",       "85",  "LKR 2,720,000",  "1", "Steady Demand")
        );
    }

    private List<ReportRowDTO> buildGrossProfitRows() {
        return Arrays.asList(
            row("Intel Core i9-14900K",    "LKR 15,620,000", "LKR 11,360,000", "LKR 4,260,000"),
            row("NVIDIA RTX 4080 OC",      "LKR 43,660,000", "LKR 33,040,000", "LKR 10,620,000"),
            row("Samsung 990 PRO 2TB SSD", "LKR 2,716,000",  "LKR 2,037,000",  "LKR 679,000")
        );
    }

    private ReportRowDTO row(String... values) {
        return new ReportRowDTO(Arrays.asList(values));
    }

    private String buildFilterDescription(ReportFilterDTO filter) {
        StringBuilder sb = new StringBuilder("Active Filter Criteria: ");
        if (filter.getReportType() != null) sb.append("Module=").append(filter.getReportType().getDisplayName());
        if (filter.getDateFrom() != null) sb.append(" | From=").append(filter.getDateFrom());
        if (filter.getDateTo() != null) sb.append(" | To=").append(filter.getDateTo());
        if (filter.getProductName() != null && !filter.getProductName().isBlank())
            sb.append(" | Query=").append(filter.getProductName());
        return sb.toString();
    }
}
