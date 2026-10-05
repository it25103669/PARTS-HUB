package com.partshub.sales.controller;

import com.partshub.sales.entity.SaleInvoice;
import com.partshub.sales.service.SalesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Sales and Billing Controller & REST API
 */
@Controller
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    // --- Page Views ---
    @GetMapping({"/sales", "/sales.html"})
    public String index(Model model) {
        model.addAttribute("pageTitle",  "Sales & Billing");
        model.addAttribute("activeMenu", "sales");
        return "sales";
    }

    // --- RESTful Endpoints ---

    @GetMapping("/api/sales")
    @ResponseBody
    public ResponseEntity<List<SaleInvoice>> getInvoices(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return ResponseEntity.ok(salesService.searchAndFilter(query, status));
    }

    @GetMapping("/api/sales/{id}")
    @ResponseBody
    public ResponseEntity<?> getInvoiceById(@PathVariable Long id) {
        return salesService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/sales")
    @ResponseBody
    public ResponseEntity<?> createInvoice(@RequestBody SaleInvoice invoice) {
        try {
            SaleInvoice created = salesService.createInvoice(invoice);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) {
                cause = cause.getCause();
            }
            String msg = e.getMessage() + " | Cause: " + cause.getMessage();
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @PutMapping("/api/sales/{id}")
    @ResponseBody
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody SaleInvoice invoice) {
        try {
            SaleInvoice updated = salesService.updateInvoice(id, invoice);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/sales/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelInvoice(@PathVariable Long id) {
        try {
            SaleInvoice cancelled = salesService.cancelInvoice(id);
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/sales/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody(required = false) Map<String, String> body,
                                          @RequestParam(required = false) String status) {
        try {
            String newStatus = status;
            if (newStatus == null && body != null) {
                newStatus = body.get("status");
            }
            if (newStatus == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
            SaleInvoice updated = salesService.updatePaymentStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/sales/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id) {
        try {
            SaleInvoice cancelled = salesService.cancelInvoice(id);
            return ResponseEntity.ok(Map.of("message", "Invoice cancelled and stock restored", "id", id, "status", cancelled.getPaymentStatus()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
