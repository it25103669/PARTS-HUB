package com.partshub.supplier.controller;

import com.partshub.supplier.entity.Supplier;
import com.partshub.supplier.entity.SupplierAction;
import com.partshub.supplier.entity.SupplierPart;
import com.partshub.supplier.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supplier Management Controller & REST API
 * Supports Full CRUD, Duplicate Prevention, Soft Delete (Deactivation),
 * Performance Actions, and Linked Spare Parts Management.
 */
@Controller
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // --- Page Views ---
    @GetMapping({"/suppliers", "/supplier-ranking", "/supplier-ranking.html"})
    public String index(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            Model model) {
        model.addAttribute("pageTitle", "Supplier Ranking & Performance");
        model.addAttribute("activeMenu", "suppliers");
        model.addAttribute("supplier", new Supplier());

        List<Supplier> allSuppliers = supplierService.getAllSuppliers();
        List<Supplier> activeSuppliers = supplierService.getActiveSuppliers();
        List<Supplier> podiumSuppliers = activeSuppliers.stream().limit(3).toList();

        model.addAttribute("suppliers", allSuppliers);
        model.addAttribute("activeSuppliers", activeSuppliers);
        model.addAttribute("podiumSuppliers", podiumSuppliers);
        model.addAttribute("currentStatusFilter", status);
        return "supplier-ranking";
    }

    private void populateCommonModel(Model model) {
        List<Supplier> allSuppliers = supplierService.getAllSuppliers();
        List<Supplier> activeSuppliers = supplierService.getActiveSuppliers();
        List<Supplier> podiumSuppliers = activeSuppliers.stream().limit(3).toList();

        model.addAttribute("suppliers", allSuppliers);
        model.addAttribute("activeSuppliers", activeSuppliers);
        model.addAttribute("podiumSuppliers", podiumSuppliers);
    }

    @GetMapping({"/suppliers/add", "/suppliers/new"})
    public String addSupplierView(Model model) {
        model.addAttribute("pageTitle", "Register Supplier - Supplier Management");
        model.addAttribute("activeMenu", "suppliers");
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("openAddModal", true);
        populateCommonModel(model);
        return "supplier-ranking";
    }

    @GetMapping({"/suppliers/edit/{id}", "/suppliers/{id}/edit"})
    public String editSupplierView(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id).orElse(new Supplier());
        model.addAttribute("pageTitle", "Edit Supplier - Supplier Management");
        model.addAttribute("activeMenu", "suppliers");
        model.addAttribute("supplier", supplier);
        model.addAttribute("editSupplierId", id);
        populateCommonModel(model);
        return "supplier-ranking";
    }

    // --- RESTful Endpoints & Form Submissions ---

    @GetMapping("/api/suppliers")
    @ResponseBody
    public ResponseEntity<List<Supplier>> getSuppliers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "score-desc") String sort) {
        return ResponseEntity.ok(supplierService.searchAndRank(query, status, sort));
    }

    @GetMapping("/api/suppliers/active")
    @ResponseBody
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierService.getActiveSuppliers());
    }

    @GetMapping({"/api/suppliers/{id}", "/suppliers/{id}"})
    @ResponseBody
    public ResponseEntity<?> getSupplierById(@PathVariable Long id) {
        return supplierService.getSupplierById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String, String> validateSupplier(Supplier s) {
        Map<String, String> errors = new HashMap<>();
        if (s == null) {
            errors.put("global", "Supplier payload cannot be empty");
            return errors;
        }
        if (s.getSupplierName() == null || s.getSupplierName().trim().isEmpty()) {
            errors.put("supplierName", "Supplier name is required.");
        } else if (s.getSupplierName().trim().length() < 2 || s.getSupplierName().trim().length() > 150) {
            errors.put("supplierName", "Supplier name must be between 2 and 150 characters.");
        }

        if (s.getSupplierCode() == null || s.getSupplierCode().trim().isEmpty()) {
            errors.put("supplierCode", "Supplier registration code is required.");
        } else if (!s.getSupplierCode().matches("^[A-Za-z0-9\\-]+$")) {
            errors.put("supplierCode", "Supplier code can only contain letters, numbers, and hyphens.");
        }

        if (s.getContactEmail() == null || s.getContactEmail().trim().isEmpty()) {
            errors.put("contactEmail", "Contact email is required.");
        } else if (!s.getContactEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.put("contactEmail", "Please enter a valid email address (e.g. procurement@vendor.lk).");
        }

        if (s.getContactPhone() != null && !s.getContactPhone().trim().isEmpty()) {
            if (!s.getContactPhone().matches("^(\\+?[0-9\\s\\-\\(\\)]{7,25})?$")) {
                errors.put("contactPhone", "Invalid contact phone number format.");
            }
        }

        if (s.getAddress() == null || s.getAddress().trim().isEmpty()) {
            errors.put("address", "Business address is required.");
        } else if (s.getAddress().trim().length() > 255) {
            errors.put("address", "Address cannot exceed 255 characters.");
        }

        return errors;
    }

    @PostMapping(value = {"/api/suppliers", "/suppliers", "/suppliers/save", "/suppliers/add"})
    public Object createSupplier(
            @RequestBody(required = false) Supplier jsonSupplier,
            @ModelAttribute Supplier formSupplier,
            @RequestHeader(value = "Accept", required = false, defaultValue = "") String acceptHeader) {
        try {
            Supplier toSave = (jsonSupplier != null && jsonSupplier.getSupplierName() != null) ? jsonSupplier : formSupplier;
            if (toSave == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid supplier payload"));
            }

            // Explicit validation checks
            Map<String, String> fieldErrors = validateSupplier(toSave);
            if (!fieldErrors.isEmpty()) {
                if (acceptHeader.contains("application/json") || jsonSupplier != null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "fieldErrors", fieldErrors));
                }
                return "redirect:/suppliers?error=Validation+failed";
            }

            Supplier created = supplierService.createSupplier(toSave);
            if (acceptHeader.contains("application/json") || jsonSupplier != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
            }
            return "redirect:/suppliers?success=created";
        } catch (IllegalArgumentException e) {
            // Duplicate registration or validation constraint violation
            if (acceptHeader.contains("application/json") || jsonSupplier != null) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "duplicate", true));
            }
            return "redirect:/suppliers?error=" + e.getMessage();
        } catch (Exception e) {
            if (acceptHeader.contains("application/json") || jsonSupplier != null) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return "redirect:/suppliers?error=" + e.getMessage();
        }
    }

    @PutMapping(value = {"/api/suppliers/{id}", "/suppliers/{id}"})
    @ResponseBody
    public ResponseEntity<?> updateSupplierPut(@PathVariable Long id, @RequestBody Supplier details) {
        try {
            Map<String, String> fieldErrors = validateSupplier(details);
            if (!fieldErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "fieldErrors", fieldErrors));
            }
            Supplier updated = supplierService.updateSupplier(id, details);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "duplicate", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = {"/api/suppliers/{id}", "/suppliers/edit/{id}", "/suppliers/update/{id}"})
    public Object updateSupplierPost(
            @PathVariable Long id,
            @RequestBody(required = false) Supplier jsonDetails,
            @ModelAttribute Supplier formDetails,
            @RequestHeader(value = "Accept", required = false, defaultValue = "") String acceptHeader) {
        try {
            Supplier details = (jsonDetails != null && jsonDetails.getSupplierName() != null) ? jsonDetails : formDetails;
            Map<String, String> fieldErrors = validateSupplier(details);
            if (!fieldErrors.isEmpty()) {
                if (acceptHeader.contains("application/json") || jsonDetails != null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "fieldErrors", fieldErrors));
                }
                return "redirect:/suppliers?error=Validation+failed";
            }

            Supplier updated = supplierService.updateSupplier(id, details);
            if (acceptHeader.contains("application/json") || jsonDetails != null) {
                return ResponseEntity.ok(updated);
            }
            return "redirect:/suppliers?success=updated";
        } catch (IllegalArgumentException e) {
            if (acceptHeader.contains("application/json") || jsonDetails != null) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "duplicate", true));
            }
            return "redirect:/suppliers?error=" + e.getMessage();
        } catch (Exception e) {
            if (acceptHeader.contains("application/json") || jsonDetails != null) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return "redirect:/suppliers?error=" + e.getMessage();
        }
    }

    // --- Soft Delete / Deactivation & Activation ---

    @RequestMapping(value = {"/api/suppliers/{id}/deactivate", "/suppliers/{id}/deactivate"},
            method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public ResponseEntity<?> deactivateSupplier(@PathVariable Long id) {
        try {
            Supplier deactivated = supplierService.deactivateSupplier(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Supplier deactivated successfully",
                    "id", id,
                    "status", deactivated.getStatus(),
                    "isActive", deactivated.getIsActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @RequestMapping(value = {"/api/suppliers/{id}/activate", "/suppliers/{id}/activate"},
            method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public ResponseEntity<?> activateSupplier(@PathVariable Long id) {
        try {
            Supplier activated = supplierService.activateSupplier(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Supplier activated successfully",
                    "id", id,
                    "status", activated.getStatus(),
                    "isActive", activated.getIsActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @RequestMapping(value = {"/api/suppliers/{id}/toggle-status", "/suppliers/{id}/toggle-status"},
            method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public ResponseEntity<?> toggleSupplierStatus(@PathVariable Long id) {
        try {
            Supplier updated = supplierService.toggleStatus(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping(value = {"/api/suppliers/{id}", "/suppliers/{id}"})
    @ResponseBody
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(Map.of("message", "Supplier deleted successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = {"/api/suppliers/{id}/delete", "/suppliers/delete/{id}", "/api/suppliers/delete/{id}"})
    public Object deleteSupplierPost(
            @PathVariable Long id,
            @RequestHeader(value = "Accept", required = false, defaultValue = "") String acceptHeader) {
        try {
            supplierService.deleteSupplier(id);
            if (acceptHeader.contains("application/json")) {
                return ResponseEntity.ok(Map.of("message", "Supplier deleted successfully", "id", id));
            }
            return "redirect:/suppliers?success=deleted";
        } catch (Exception e) {
            if (acceptHeader.contains("application/json")) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return "redirect:/suppliers?error=" + e.getMessage();
        }
    }

    // --- Performance Action Tracking ---

    @PostMapping("/api/suppliers/{id}/actions")
    @ResponseBody
    public ResponseEntity<?> recordAction(@PathVariable Long id, @RequestBody SupplierAction action) {
        try {
            Supplier updated = supplierService.recordAction(id, action);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/suppliers/{id}/actions")
    @ResponseBody
    public ResponseEntity<List<SupplierAction>> getActions(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierActions(id));
    }

    // --- Linked Spare Parts & Pricing / Delivery Comparison ---

    @GetMapping("/api/suppliers/{id}/parts")
    @ResponseBody
    public ResponseEntity<List<SupplierPart>> getSupplierParts(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getPartsBySupplier(id));
    }

    @PostMapping("/api/suppliers/{id}/parts")
    @ResponseBody
    public ResponseEntity<?> linkPart(@PathVariable Long id, @RequestBody SupplierPart part) {
        try {
            SupplierPart linked = supplierService.linkPartToSupplier(id, part);
            return ResponseEntity.status(HttpStatus.CREATED).body(linked);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/suppliers/{id}/parts/{partId}")
    @ResponseBody
    public ResponseEntity<?> unlinkPart(@PathVariable Long id, @PathVariable Long partId) {
        try {
            supplierService.unlinkPartFromSupplier(id, partId);
            return ResponseEntity.ok(Map.of("message", "Part unlinked from supplier successfully", "partId", partId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/suppliers/parts/comparison")
    @ResponseBody
    public ResponseEntity<List<SupplierPart>> getPricingComparison(
            @RequestParam(required = false, defaultValue = "ALL") String category) {
        return ResponseEntity.ok(supplierService.getPricingComparison(category));
    }
}
