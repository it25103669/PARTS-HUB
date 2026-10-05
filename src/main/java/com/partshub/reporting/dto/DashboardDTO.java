package com.partshub.reporting.dto;

import java.util.List;

/**
 * Top-level DTO passed to the dashboard template.
 * Holds all summary cards, chart data sets, and the activity feed.
 */
public class DashboardDTO {

    private List<SummaryCardDTO> summaryCards;

    // Chart data objects (serialized to JSON by the controller for Chart.js)
    private ChartDataDTO salesChart;
    private ChartDataDTO stockChart;
    private ChartDataDTO purchaseVsSalesChart;
    private ChartDataDTO topProductsChart;
    private ChartDataDTO supplierPerformanceChart;
    private ChartDataDTO returnsSummaryChart;

    private List<ActivityItemDTO> recentActivities;

    public DashboardDTO() {}

    public List<SummaryCardDTO> getSummaryCards() { return summaryCards; }
    public void setSummaryCards(List<SummaryCardDTO> summaryCards) {
        this.summaryCards = summaryCards;
    }

    public ChartDataDTO getSalesChart() { return salesChart; }
    public void setSalesChart(ChartDataDTO salesChart) { this.salesChart = salesChart; }

    public ChartDataDTO getStockChart() { return stockChart; }
    public void setStockChart(ChartDataDTO stockChart) { this.stockChart = stockChart; }

    public ChartDataDTO getPurchaseVsSalesChart() { return purchaseVsSalesChart; }
    public void setPurchaseVsSalesChart(ChartDataDTO purchaseVsSalesChart) {
        this.purchaseVsSalesChart = purchaseVsSalesChart;
    }

    public ChartDataDTO getTopProductsChart() { return topProductsChart; }
    public void setTopProductsChart(ChartDataDTO topProductsChart) {
        this.topProductsChart = topProductsChart;
    }

    public ChartDataDTO getSupplierPerformanceChart() { return supplierPerformanceChart; }
    public void setSupplierPerformanceChart(ChartDataDTO supplierPerformanceChart) {
        this.supplierPerformanceChart = supplierPerformanceChart;
    }

    public ChartDataDTO getReturnsSummaryChart() { return returnsSummaryChart; }
    public void setReturnsSummaryChart(ChartDataDTO returnsSummaryChart) {
        this.returnsSummaryChart = returnsSummaryChart;
    }

    public List<ActivityItemDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityItemDTO> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
