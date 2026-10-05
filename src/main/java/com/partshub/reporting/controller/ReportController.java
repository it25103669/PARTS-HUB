package com.partshub.reporting.controller;

import com.partshub.reporting.dto.ReportFilterDTO;
import com.partshub.reporting.dto.ReportRowDTO;
import com.partshub.reporting.dto.ReportSummaryDTO;
import com.partshub.reporting.model.ReportType;
import com.partshub.reporting.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles report listing, filtering, and export routes.
 * Thin controller: all logic lives in ReportService.
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Show the reports page with an empty filter form (no results yet). */
    @GetMapping
    public String reportsPage(Model model) {
        if (!model.containsAttribute("filter")) {
            model.addAttribute("filter", new ReportFilterDTO());
        }
        model.addAttribute("reportTypes",  ReportType.values());
        model.addAttribute("pageTitle",    "Reports");
        model.addAttribute("activeMenu",   "reports");
        model.addAttribute("hasResults",   false);
        return "reports/reports";
    }

    /** Apply filters and display results table. */
    @PostMapping
    public String applyFilters(@ModelAttribute ReportFilterDTO filter, Model model) {
        ReportSummaryDTO summary      = reportService.getReportSummary(filter);
        List<String>     headers      = reportService.getColumnHeaders(filter);
        List<ReportRowDTO> rows       = reportService.generateReport(filter);

        model.addAttribute("filter",       filter);
        model.addAttribute("reportTypes",  ReportType.values());
        model.addAttribute("summary",      summary);
        model.addAttribute("columnHeaders",headers);
        model.addAttribute("rows",         rows);
        model.addAttribute("hasResults",   true);
        model.addAttribute("pageTitle",    "Reports");
        model.addAttribute("activeMenu",   "reports");
        return "reports/reports";
    }

    /** Export report as PDF. */
    @RequestMapping(value = {"/export/pdf", "/pdf"}, method = {RequestMethod.GET, RequestMethod.POST})
    public void exportPdf(@ModelAttribute ReportFilterDTO filter, HttpServletResponse response) {
        reportService.exportPdf(filter, response);
    }

    /** Export report as Excel. */
    @RequestMapping(value = {"/export/excel", "/excel"}, method = {RequestMethod.GET, RequestMethod.POST})
    public void exportExcel(@ModelAttribute ReportFilterDTO filter, HttpServletResponse response) {
        reportService.exportExcel(filter, response);
    }
}
