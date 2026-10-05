package com.partshub.purchase.controller;

import com.partshub.purchase.entity.PurchaseOrder;
import com.partshub.purchase.service.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Purchase and Receiving Management Controller & REST API
 */
@Controller
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    // --- Page Views ---
    @GetMapping({"/purchases", "/purchases.html"})
    public String index(Model model) {
        model.addAttribute("pageTitle",  "Purchase & Receiving");
        model.addAttribute("activeMenu", "purchases");
        model.addAttribute("stats",      purchaseService.getPurchaseStats());
        return "purchases";
    }

    // --- RESTful Endpoints ---

    @GetMapping("/api/purchases/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(purchaseService.getPurchaseStats());
    }

    @GetMapping("/api/purchases")
    @ResponseBody
    public ResponseEntity<List<PurchaseOrder>> getOrders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return ResponseEntity.ok(purchaseService.searchAndFilter(query, status));
    }

    @GetMapping("/api/purchases/{id}")
    @ResponseBody
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return purchaseService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/purchases")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody PurchaseOrder order) {
        try {
            PurchaseOrder created = purchaseService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/purchases/{id}")
    @ResponseBody
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody PurchaseOrder details) {
        try {
            PurchaseOrder updated = purchaseService.updateOrder(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/purchases/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            purchaseService.deleteOrder(id);
            return ResponseEntity.ok(Map.of("message", "Purchase order deleted successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/purchases/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            PurchaseOrder cancelled = purchaseService.cancelOrder(id);
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/purchases/{id}/receive")
    @ResponseBody
    public ResponseEntity<?> receiveOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        try {
            List<Map<String, Object>> items = null;
            String notes = "";
            if (body != null) {
                if (body.get("items") instanceof List) {
                    items = (List<Map<String, Object>>) body.get("items");
                }
                if (body.get("notes") != null) {
                    notes = body.get("notes").toString();
                }
            }
            PurchaseOrder updated = purchaseService.receiveOrder(id, items, notes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
