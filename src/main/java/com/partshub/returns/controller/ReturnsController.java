package com.partshub.returns.controller;

import com.partshub.returns.dto.SaleLookupDTO;
import com.partshub.returns.entity.ReturnClaim;
import com.partshub.returns.service.ReturnsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Returns and Warranty Management Controller & REST API
 */
@Controller
public class ReturnsController {

    private final ReturnsService returnsService;

    public ReturnsController(ReturnsService returnsService) {
        this.returnsService = returnsService;
    }

    // --- Page Views ---
    @GetMapping({"/returns", "/returns.html"})
    public String index(Model model) {
        model.addAttribute("pageTitle",  "Returns & Warranty");
        model.addAttribute("activeMenu", "returns");
        return "returns";
    }

    // --- RESTful Endpoints ---

    @GetMapping("/api/returns")
    @ResponseBody
    public ResponseEntity<List<ReturnClaim>> getClaims(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return ResponseEntity.ok(returnsService.searchAndFilter(query, type, status));
    }

    @GetMapping("/api/returns/lookup-sale")
    @ResponseBody
    public ResponseEntity<?> lookupSale(
            @RequestParam String invoiceNumber,
            @RequestParam(required = false) String customerName) {
        try {
            SaleLookupDTO dto = returnsService.lookupSale(invoiceNumber, customerName);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/returns/{id}")
    @ResponseBody
    public ResponseEntity<?> getClaimById(@PathVariable Long id) {
        return returnsService.getClaimById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/returns")
    @ResponseBody
    public ResponseEntity<?> createClaim(@RequestBody ReturnClaim claim) {
        try {
            ReturnClaim created = returnsService.createClaim(claim);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/returns/{id}")
    @ResponseBody
    public ResponseEntity<?> updateClaim(@PathVariable Long id, @RequestBody ReturnClaim details) {
        try {
            ReturnClaim updated = returnsService.updateClaim(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/returns/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody(required = false) Map<String, String> body,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String resolution) {
        try {
            String newStatus = status;
            String newResolution = resolution;
            if (body != null) {
                if (newStatus == null) newStatus = body.get("status");
                if (newResolution == null) newResolution = body.get("resolution");
            }
            if (newStatus == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
            ReturnClaim updated = returnsService.updateStatus(id, newStatus, newResolution);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/returns/{id}/resolve-supplier-claim")
    @ResponseBody
    public ResponseEntity<?> resolveSupplierClaim(@PathVariable Long id,
                                                   @RequestBody(required = false) Map<String, String> body) {
        try {
            String resolution = body != null ? body.get("resolution") : "Supplier Replaced / Credited";
            String notes = body != null ? body.get("notes") : "";
            ReturnClaim updated = returnsService.resolveSupplierClaim(id, resolution, notes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/returns/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteClaim(@PathVariable Long id) {
        try {
            returnsService.deleteClaim(id);
            return ResponseEntity.ok(Map.of("message", "Return claim deleted successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
