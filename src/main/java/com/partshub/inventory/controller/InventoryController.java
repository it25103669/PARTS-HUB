package com.partshub.inventory.controller;

import com.partshub.inventory.entity.InventoryItem;
import com.partshub.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Inventory Management – Controller and REST API with Multipart Image Upload Support
 */
@Controller
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // --- Page View Mappings ---
    @GetMapping({"/inventory", "/inventory.html"})
    public String index(Model model) {
        model.addAttribute("pageTitle", "Inventory Management");
        model.addAttribute("activeMenu", "inventory");
        model.addAttribute("items", inventoryService.getAllItems());
        model.addAttribute("totalValue", inventoryService.getTotalInventoryValue());
        return "inventory";
    }

    @GetMapping({"/inventory/add", "/inventory/new"})
    public String addForm(Model model) {
        model.addAttribute("pageTitle", "Add Inventory Item");
        model.addAttribute("activeMenu", "inventory");
        model.addAttribute("item", new InventoryItem());
        model.addAttribute("totalValue", inventoryService.getTotalInventoryValue());
        return "inventory";
    }

    @GetMapping({"/inventory/edit/{id}", "/inventory/{id}/edit"})
    public String editForm(@PathVariable Long id, Model model) {
        InventoryItem item = inventoryService.getItemById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid inventory ID: " + id));
        model.addAttribute("pageTitle", "Edit Inventory Item");
        model.addAttribute("activeMenu", "inventory");
        model.addAttribute("item", item);
        model.addAttribute("totalValue", inventoryService.getTotalInventoryValue());
        return "inventory";
    }

    // --- RESTful API Endpoints for Full CRUD ---

    @GetMapping("/api/inventory/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInventoryStats() {
        List<InventoryItem> all = inventoryService.getAllItems();
        long activeSkus = all.size();
        long lowStock = all.stream().filter(i -> i.getQtyOnHand() != null && i.getReorderLevel() != null && i.getQtyOnHand() <= i.getReorderLevel() && i.getQtyOnHand() > 0).count();
        long outOfStock = all.stream().filter(i -> i.getQtyOnHand() != null && i.getQtyOnHand() == 0).count();
        java.math.BigDecimal totalVal = inventoryService.getTotalInventoryValue();

        return ResponseEntity.ok(Map.of(
                "activeSkus", activeSkus,
                "lowStock", lowStock,
                "outOfStock", outOfStock,
                "totalValue", totalVal != null ? totalVal : java.math.BigDecimal.ZERO
        ));
    }

    @GetMapping("/api/inventory")
    @ResponseBody
    public ResponseEntity<List<InventoryItem>> getItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(required = false, defaultValue = "ALL") String brand,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "qty-desc") String sort) {
        return ResponseEntity.ok(inventoryService.searchAndFilter(query, category, brand, status, sort));
    }

    @GetMapping("/api/inventory/approved")
    @ResponseBody
    public ResponseEntity<List<InventoryItem>> getApprovedItems() {
        return ResponseEntity.ok(inventoryService.getApprovedItems());
    }

    @GetMapping("/api/inventory/category-counts")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getCategoryCounts() {
        return ResponseEntity.ok(inventoryService.getApprovedCategoryCounts());
    }

    @GetMapping("/api/inventory/pending")
    @ResponseBody
    public ResponseEntity<List<InventoryItem>> getPendingItems() {
        return ResponseEntity.ok(inventoryService.getPendingItems());
    }

    @PostMapping({"/api/inventory/{id}/approve", "/inventory/{id}/approve"})
    @ResponseBody
    public ResponseEntity<?> approveItem(@PathVariable Long id) {
        try {
            InventoryItem approved = inventoryService.approveItem(id);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping({"/api/inventory/{id}/reject", "/inventory/{id}/reject"})
    @ResponseBody
    public ResponseEntity<?> rejectItem(@PathVariable Long id) {
        try {
            InventoryItem rejected = inventoryService.rejectItem(id);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/inventory/{id}")
    @ResponseBody
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        return inventoryService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Step 5: Stock Update Endpoint with Negative Protection
    @PostMapping(value = {"/api/inventory/{id}/stock", "/api/inventory/stock-update"})
    @ResponseBody
    public ResponseEntity<?> updateStock(
            @PathVariable(required = false) Long id,
            @RequestParam(required = false) Long itemId,
            @RequestParam(defaultValue = "0") int delta,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Long targetId = id != null ? id : itemId;
            if (body != null) {
                if (targetId == null && body.containsKey("id")) {
                    targetId = Long.parseLong(body.get("id").toString());
                }
                if (body.containsKey("delta")) {
                    delta = Integer.parseInt(body.get("delta").toString());
                }
            }
            if (targetId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Item ID is required for stock update."));
            }
            InventoryItem updated = inventoryService.updateStock(targetId, delta);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Step 8: Restock Request Trigger Endpoint
    @PostMapping("/api/inventory/{id}/restock-request")
    @ResponseBody
    public ResponseEntity<?> triggerRestockRequest(@PathVariable Long id) {
        try {
            inventoryService.triggerRestockRequest(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Restocking request successfully generated for purchasing agents."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 1. Create with JSON body
    @PostMapping(value = "/api/inventory", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createItemJson(@RequestBody InventoryItem item) {
        try {
            InventoryItem created = inventoryService.createItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. Create with Multipart Form Data (Image Upload)
    @PostMapping(value = {"/api/inventory", "/inventory/save", "/inventory/add", "/inventory"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> createItemMultipart(
            @ModelAttribute InventoryItem item,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String relativeImagePath = saveUploadedImage(imageFile);
                item.setImage(relativeImagePath);
            }
            InventoryItem created = inventoryService.createItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 3. Update with JSON body
    @PutMapping(value = "/api/inventory/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateItemJson(@PathVariable Long id, @RequestBody InventoryItem details) {
        try {
            InventoryItem updated = inventoryService.updateItem(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. Update with Multipart Form Data (Image Upload)
    @RequestMapping(value = {"/api/inventory/{id}", "/inventory/edit/{id}", "/inventory/update/{id}"},
            method = {RequestMethod.POST, RequestMethod.PUT},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateItemMultipart(
            @PathVariable Long id,
            @ModelAttribute InventoryItem details,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String relativeImagePath = saveUploadedImage(imageFile);
                details.setImage(relativeImagePath);
            }
            InventoryItem updated = inventoryService.updateItem(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. Delete Item
    @RequestMapping(value = {"/api/inventory/{id}", "/api/inventory/{id}/delete", "/inventory/delete/{id}"},
            method = {RequestMethod.DELETE, RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            inventoryService.deleteItem(id);
            return ResponseEntity.ok(Map.of("message", "Inventory item deleted successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Writes uploaded MultipartFile to src/main/resources/static/images/products using a unique filename (UUID + extension).
     * Also mirrors the file into target/classes/static/images/products if running in exploded dev mode.
     * Returns the relative path e.g. "images/products/<uuid>.ext".
     */
    private String saveUploadedImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Locate upload directory
        Path uploadDir = Paths.get("src", "main", "resources", "static", "images", "products").toAbsolutePath().normalize();
        if (!Files.exists(uploadDir.getParent())) {
            Path altDir = Paths.get("Project", "src", "main", "resources", "static", "images", "products").toAbsolutePath().normalize();
            if (Files.exists(altDir.getParent())) {
                uploadDir = altDir;
            }
        }

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Determine extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = ".jpg";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path destination = uploadDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Also copy to target/classes if available for instant runtime static pickup
        try {
            Path targetClassesDir = Paths.get("target", "classes", "static", "images", "products").toAbsolutePath().normalize();
            if (!Files.exists(targetClassesDir.getParent())) {
                Path altTarget = Paths.get("Project", "target", "classes", "static", "images", "products").toAbsolutePath().normalize();
                if (Files.exists(altTarget.getParent())) {
                    targetClassesDir = altTarget;
                }
            }
            if (Files.exists(targetClassesDir)) {
                Files.copy(destination, targetClassesDir.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
        }

        return "images/products/" + uniqueFilename;
    }
}
