package com.partshub.reporting.service;

import com.partshub.reporting.campaign.service.PromotionalCampaignService;
import com.partshub.reporting.dto.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * MOCK DATA PROVIDER – Dashboard
 * ---------------------------------------------------------------
 * All data returned here is hard-coded for demonstration purposes.
 * Replace this class (or swap the @Service annotation) with
 * DatabaseDashboardDataProvider once real repositories are ready.
 * ---------------------------------------------------------------
 */
@Service
public class MockDashboardDataProvider implements DashboardService {

    private final PromotionalCampaignService campaignService;

    /**
     * Constructor injection of PromotionalCampaignService.
     * This allows the dashboard to show the real active campaign count
     * from the database instead of a hardcoded mock value.
     */
    public MockDashboardDataProvider(PromotionalCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Override
    public DashboardDTO getDashboardData() {
        DashboardDTO dto = new DashboardDTO();
        dto.setSummaryCards(buildSummaryCards());
        dto.setSalesChart(buildSalesChart());
        dto.setStockChart(buildStockChart());
        dto.setPurchaseVsSalesChart(buildPurchaseVsSalesChart());
        dto.setTopProductsChart(buildTopProductsChart());
        dto.setSupplierPerformanceChart(buildSupplierPerformanceChart());
        dto.setReturnsSummaryChart(buildReturnsSummaryChart());
        dto.setRecentActivities(buildRecentActivities());
        return dto;
    }

    // ── Summary Cards ──────────────────────────────────────────

    private List<SummaryCardDTO> buildSummaryCards() {
        return Arrays.asList(
            new SummaryCardDTO("Total Products",      "1,247",    "bi-box-seam",        "accent-blue",    "+12 this month",      true),
            new SummaryCardDTO("Low Stock Items",     "23",       "bi-exclamation-triangle", "accent-warning", "4 critical items", false),
            new SummaryCardDTO("Total Sales",         "$48,320",  "bi-cart-check",      "accent-blue",    "+8.3% vs last month", true),
            new SummaryCardDTO("Total Purchases",     "$32,150",  "bi-bag",             "accent-blue",    "+3.1% vs last month", true),
            new SummaryCardDTO("Active Suppliers",    "18",       "bi-truck",           "accent-blue",    "2 new this quarter",  true),
            new SummaryCardDTO("Pending Returns",     "7",        "bi-arrow-return-left","accent-danger", "3 awaiting review",  false),
            new SummaryCardDTO("Est. Gross Profit",   "$16,170",  "bi-graph-up-arrow",  "accent-success", "+5.2% vs last month", true),
            new SummaryCardDTO("Active Promotions",
                    String.valueOf(campaignService.countActiveCampaigns()),
                    "bi-megaphone",
                    "accent-blue",
                    "Live from database",
                    true)
        );
    }

    // ── Charts ─────────────────────────────────────────────────

    private ChartDataDTO buildSalesChart() {
        ChartDataDTO chart = new ChartDataDTO();
        chart.setChartType("line");
        chart.setLabels(Arrays.asList("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"));
        chart.setDatasetLabels(Arrays.asList("Monthly Sales ($)"));
        chart.setDatasets(Arrays.asList(
            Arrays.asList(3200, 4100, 3800, 5200, 4700, 5800, 6200, 5400, 6900, 7100, 6500, 8200)
        ));
        chart.setBackgroundColors(Arrays.asList("#2563EB"));
        return chart;
    }

    private ChartDataDTO buildStockChart() {
        ChartDataDTO chart = new ChartDataDTO();
        chart.setChartType("doughnut");
        chart.setLabels(Arrays.asList("In Stock", "Low Stock", "Out of Stock"));
        chart.setDatasetLabels(Arrays.asList("Stock Status"));
        chart.setDatasets(Arrays.asList(Arrays.asList(892, 23, 12)));
        chart.setBackgroundColors(Arrays.asList("#22C55E", "#F59E0B", "#EF4444"));
        return chart;
    }

    private ChartDataDTO buildPurchaseVsSalesChart() {
        ChartDataDTO chart = new ChartDataDTO();
        chart.setChartType("bar");
        chart.setLabels(Arrays.asList("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"));
        chart.setDatasetLabels(Arrays.asList("Purchases ($)", "Sales ($)"));
        chart.setDatasets(Arrays.asList(
            Arrays.asList(2100, 2800, 2500, 3400, 3100, 3900, 4100, 3600, 4600, 4800, 4300, 5500),
            Arrays.asList(3200, 4100, 3800, 5200, 4700, 5800, 6200, 5400, 6900, 7100, 6500, 8200)
        ));
        chart.setBackgroundColors(Arrays.asList("#3B82F6", "#22C55E"));
        return chart;
    }

    private ChartDataDTO buildTopProductsChart() {
        ChartDataDTO chart = new ChartDataDTO();
        chart.setChartType("bar");
        chart.setLabels(Arrays.asList(
            "Intel Core i7-14700K",
            "NVIDIA RTX 4070",
            "Samsung 970 EVO 1TB",
            "Corsair Vengeance 32GB",
            "ASUS ROG Strix B650"
        ));
        chart.setDatasetLabels(Arrays.asList("Units Sold"));
        chart.setDatasets(Arrays.asList(Arrays.asList(142, 118, 97, 85, 73)));
        chart.setBackgroundColors(Arrays.asList("#3B82F6"));
        return chart;
    }

    private ChartDataDTO buildSupplierPerformanceChart() {
        ChartDataDTO chart = new ChartDataDTO();
        chart.setChartType("bar");
        chart.setLabels(Arrays.asList("TechParts Ltd", "ByteSupply Co.", "CoreDist Inc.", "PCWorld Wholesale", "DigiSource"));
        chart.setDatasetLabels(Arrays.asList("Reliability Score (%)"));
        chart.setDatasets(Arrays.asList(Arrays.asList(92, 87, 78, 95, 83)));
        chart.setBackgroundColors(Arrays.asList("#2563EB"));
        return chart;
    }

    private ChartDataDTO buildReturnsSummaryChart() {
        ChartDataDTO chart = new ChartDataDTO();
        chart.setChartType("doughnut");
        chart.setLabels(Arrays.asList("Approved", "Pending", "Rejected"));
        chart.setDatasetLabels(Arrays.asList("Returns"));
        chart.setDatasets(Arrays.asList(Arrays.asList(45, 7, 12)));
        chart.setBackgroundColors(Arrays.asList("#22C55E", "#F59E0B", "#EF4444"));
        return chart;
    }

    // ── Recent Activity ─────────────────────────────────────────

    private List<ActivityItemDTO> buildRecentActivities() {
        return Arrays.asList(
            new ActivityItemDTO("Sale #INV-2024-0891 completed – $1,240.00",         "2 hours ago",   "bi-cart-check",         "badge-success"),
            new ActivityItemDTO("Purchase order PO-2024-0156 received from TechParts Ltd", "4 hours ago", "bi-bag-check",    "badge-blue"),
            new ActivityItemDTO("Low stock alert: Intel Core i7-14700K (qty: 3)",    "6 hours ago",   "bi-exclamation-triangle","badge-warning"),
            new ActivityItemDTO("Return RET-2024-0042 approved – Corsair Vengeance", "Yesterday",     "bi-arrow-return-left",  "badge-success"),
            new ActivityItemDTO("Supplier ByteSupply Co. updated delivery schedule", "Yesterday",     "bi-truck",              "badge-blue"),
            new ActivityItemDTO("Sale #INV-2024-0888 completed – $870.00",           "2 days ago",    "bi-cart-check",         "badge-success"),
            new ActivityItemDTO("New product added: ASUS ROG Strix B650-E",          "2 days ago",    "bi-plus-circle",        "badge-blue")
        );
    }
}
