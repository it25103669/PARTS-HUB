package com.partshub.reporting.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.partshub.inventory.service.InventoryService;
import com.partshub.purchase.service.PurchaseService;
import com.partshub.reporting.campaign.service.PromotionalCampaignService;
import com.partshub.reporting.dto.DashboardDTO;
import com.partshub.reporting.service.DashboardService;
import com.partshub.returns.service.ReturnsService;
import com.partshub.sales.service.SalesService;
import com.partshub.supplier.service.SupplierService;
import com.partshub.reporting.dto.ReportFilterDTO;
import com.partshub.reporting.model.ReportType;
import com.partshub.reporting.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Handles the main dashboard routes and stats API.
 */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;
    private final SupplierService supplierService;
    private final PurchaseService purchaseService;
    private final SalesService salesService;
    private final ReturnsService returnsService;
    private final ReportService reportService;
    private final PromotionalCampaignService campaignService;

    public DashboardController(DashboardService dashboardService,
                               ObjectMapper objectMapper,
                               InventoryService inventoryService,
                               SupplierService supplierService,
                               PurchaseService purchaseService,
                               SalesService salesService,
                               ReturnsService returnsService,
                               ReportService reportService,
                               PromotionalCampaignService campaignService) {
        this.dashboardService = dashboardService;
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
        this.supplierService = supplierService;
        this.purchaseService = purchaseService;
        this.salesService = salesService;
        this.returnsService = returnsService;
        this.reportService = reportService;
        this.campaignService = campaignService;
    }

    @GetMapping({"/dashboard", "/dashboard.html"})
    public String dashboard(Model model) throws JsonProcessingException {
        DashboardDTO data = dashboardService.getDashboardData();

        model.addAttribute("summaryCards",   data.getSummaryCards());
        model.addAttribute("recentActivities", data.getRecentActivities());

        // Serialize chart objects to JSON strings for Chart.js
        model.addAttribute("salesChartJson",            objectMapper.writeValueAsString(data.getSalesChart()));
        model.addAttribute("stockChartJson",            objectMapper.writeValueAsString(data.getStockChart()));
        model.addAttribute("purchaseVsSalesChartJson",  objectMapper.writeValueAsString(data.getPurchaseVsSalesChart()));
        model.addAttribute("topProductsChartJson",      objectMapper.writeValueAsString(data.getTopProductsChart()));
        model.addAttribute("supplierChartJson",         objectMapper.writeValueAsString(data.getSupplierPerformanceChart()));
        model.addAttribute("returnsChartJson",          objectMapper.writeValueAsString(data.getReturnsSummaryChart()));

        model.addAttribute("pageTitle",  "Dashboard & Reports");
        model.addAttribute("activeMenu", "dashboard");

        return "dashboard";
    }

    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLiveStats(@RequestParam(required = false, defaultValue = "6M") String period) {
        BigDecimal revenue = salesService.getTotalPaidRevenue();
        BigDecimal inventoryValuation = inventoryService.getTotalInventoryValue();
        long inventoryCount = inventoryService.countTotal();
        long activeSuppliersCount = supplierService.getActiveSuppliers().size();
        if (activeSuppliersCount == 0) {
            activeSuppliersCount = supplierService.countTotal();
        }

        long openPOs = purchaseService.searchAndFilter(null, "PENDING").size() 
                     + purchaseService.searchAndFilter(null, "DRAFT").size()
                     + purchaseService.searchAndFilter(null, "SENT").size();
        
        long pendingInvoices = salesService.searchAndFilter(null, "UNPAID").size() 
                             + salesService.searchAndFilter(null, "PENDING").size();
        
        long pendingOrders = openPOs + pendingInvoices;
        if (pendingOrders == 0) {
            pendingOrders = purchaseService.countTotal() + salesService.countTotal();
        }

        long activePromotions = campaignService != null ? campaignService.countActiveCampaigns() : 0;

        return ResponseEntity.ok(Map.of(
                "totalRevenue", revenue != null ? revenue : BigDecimal.ZERO,
                "totalInventoryValuation", inventoryValuation != null ? inventoryValuation : BigDecimal.ZERO,
                "inventoryItems", inventoryCount,
                "suppliersCount", activeSuppliersCount,
                "pendingOrdersCount", pendingOrders,
                "salesCount", salesService.countTotal(),
                "purchasesCount", purchaseService.countTotal(),
                "returnsCount", returnsService.countTotal(),
                "activePromotionsCount", activePromotions
        ));
    }

    @RequestMapping(value = {"/api/dashboard/export/pdf", "/dashboard/export/pdf"}, method = {RequestMethod.GET, RequestMethod.POST})
    public void exportDashboardPdf(@RequestParam(required = false, defaultValue = "INVENTORY") String type, HttpServletResponse response) {
        ReportFilterDTO filter = new ReportFilterDTO();
        try {
            filter.setReportType(ReportType.valueOf(type.toUpperCase()));
        } catch (Exception e) {
            filter.setReportType(ReportType.INVENTORY);
        }
        reportService.exportPdf(filter, response);
    }

    @RequestMapping(value = {"/api/dashboard/export/excel", "/dashboard/export/excel"}, method = {RequestMethod.GET, RequestMethod.POST})
    public void exportDashboardExcel(@RequestParam(required = false, defaultValue = "INVENTORY") String type, HttpServletResponse response) {
        ReportFilterDTO filter = new ReportFilterDTO();
        try {
            filter.setReportType(ReportType.valueOf(type.toUpperCase()));
        } catch (Exception e) {
            filter.setReportType(ReportType.INVENTORY);
        }
        reportService.exportExcel(filter, response);
    }
}

