package com.partshub.inventory.service;

import com.partshub.inventory.entity.InventoryItem;
import com.partshub.inventory.repository.InventoryItemRepository;
import com.partshub.notification.entity.Notification;
import com.partshub.notification.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryItemRepository repository;
    private final NotificationRepository notificationRepository;

    public InventoryServiceImpl(InventoryItemRepository repository,
                                NotificationRepository notificationRepository) {
        this.repository = repository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getAllItems() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> searchAndFilter(String query, String category, String status, String sort) {
        return searchAndFilter(query, category, null, status, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> searchAndFilter(String query, String category, String brand, String status, String sort) {
        List<InventoryItem> list = repository.searchAndFilter(query, category, brand, status);

        if (sort != null) {
            switch (sort) {
                case "qty-desc":
                    list.sort(Comparator.comparing(InventoryItem::getQtyOnHand).reversed());
                    break;
                case "qty-asc":
                    list.sort(Comparator.comparing(InventoryItem::getQtyOnHand));
                    break;
                case "name-asc":
                    list.sort(Comparator.comparing(InventoryItem::getPartName, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "val-desc":
                    list.sort(Comparator.comparing(InventoryItem::getTotalValue).reversed());
                    break;
                default:
                    list.sort(Comparator.comparing(InventoryItem::getId).reversed());
            }
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getItemById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getItemBySku(String skuCode) {
        return repository.findBySkuCode(skuCode);
    }

    @jakarta.annotation.PostConstruct
    public void initBackfillApprovalStatus() {
        try {
            List<InventoryItem> all = repository.findAll();
            for (InventoryItem item : all) {
                if (item.getApprovalStatus() == null || item.getApprovalStatus().isBlank()) {
                    item.setApprovalStatus("APPROVED");
                    repository.save(item);
                }
            }
        } catch (Exception e) {
            logger.warn("Approval status backfill deferred: {}", e.getMessage());
        }
    }

    @Override
    public InventoryItem createItem(InventoryItem item) {
        // Step 3: Validate required fields
        validateItem(item);

        // Step 4: Duplicate checking
        if (item.getPartName() != null && item.getBrand() != null &&
            repository.existsByPartNameIgnoreCaseAndBrandIgnoreCase(item.getPartName().trim(), item.getBrand().trim())) {
            return repository.findByPartNameIgnoreCase(item.getPartName().trim()).orElse(item);
        }

        if (item.getSkuCode() != null && !item.getSkuCode().trim().isEmpty() &&
            repository.existsBySkuCodeIgnoreCase(item.getSkuCode().trim())) {
            return repository.findBySkuCode(item.getSkuCode().trim()).orElse(item);
        }

        if (item.getSkuCode() == null || item.getSkuCode().trim().isEmpty()) {
            item.setSkuCode(generateUniqueSku(item.getCategory()));
        }

        if (item.getApprovalStatus() == null || item.getApprovalStatus().isBlank()) {
            item.setApprovalStatus("PENDING");
        }

        item.calculateStatus();
        InventoryItem saved = repository.save(item);

        if ("PENDING".equalsIgnoreCase(saved.getApprovalStatus())) {
            try {
                Notification notif = new Notification(
                        "ROLE_OWNER",
                        "New Item Pending Approval: " + saved.getPartName(),
                        "Storekeeper added a new spare part '" + saved.getPartName() + "' (SKU: " + saved.getSkuCode() + "). Business Owner approval required before catalog publishing.",
                        "WARNING",
                        "inventory"
                );
                notificationRepository.save(notif);
            } catch (Exception e) {
                logger.error("Failed to send pending approval notification: {}", e.getMessage());
            }
        }

        // Step 7 & 8 & 9: Low-stock threshold check and alert generation
        checkAndGenerateLowStockAlert(saved);

        return saved;
    }

    @Override
    public InventoryItem updateItem(Long id, InventoryItem details) {
        InventoryItem existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));

        validateItem(details);

        existing.setPartName(details.getPartName());
        if (details.getSkuCode() != null && !details.getSkuCode().trim().isEmpty()) {
            existing.setSkuCode(details.getSkuCode());
        }
        existing.setCategory(details.getCategory());
        if (details.getBrand() != null) existing.setBrand(details.getBrand());
        if (details.getCompatibility() != null) existing.setCompatibility(details.getCompatibility());
        existing.setSupplierName(details.getSupplierName());
        existing.setQtyOnHand(details.getQtyOnHand());
        existing.setReorderLevel(details.getReorderLevel());
        existing.setUnitCost(details.getUnitCost());
        existing.setSellingPrice(details.getSellingPrice());
        existing.setWarehouseLocation(details.getWarehouseLocation());
        existing.setDescription(details.getDescription());
        if (details.getImage() != null && !details.getImage().isBlank()) {
            existing.setImage(details.getImage());
        }
        existing.calculateStatus();

        InventoryItem saved = repository.save(existing);
        checkAndGenerateLowStockAlert(saved);
        return saved;
    }

    @Override
    public InventoryItem updateStock(Long id, int delta) {
        InventoryItem item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));

        // Step 5: Negative quantity protection
        int newQty = item.getQtyOnHand() + delta;
        if (newQty < 0) {
            throw new IllegalArgumentException("Stock update rejected! Updating stock for '" + item.getPartName() 
                    + "' by " + delta + " would cause a negative stock balance (" + newQty + "). Current available stock: " + item.getQtyOnHand() + " units.");
        }

        item.setQtyOnHand(newQty);
        item.calculateStatus();
        InventoryItem saved = repository.save(item);

        // Step 7, 8 & 9: Threshold monitoring & notification
        checkAndGenerateLowStockAlert(saved);
        return saved;
    }

    @Override
    public void adjustStock(String partNameOrSku, int delta) {
        Optional<InventoryItem> opt = repository.findBySkuCode(partNameOrSku);
        if (opt.isEmpty()) {
            opt = repository.findByPartNameIgnoreCase(partNameOrSku);
        }
        if (opt.isPresent()) {
            updateStock(opt.get().getId(), delta);
        } else {
            throw new EntityNotFoundException("Spare part not found for stock adjustment: " + partNameOrSku);
        }
    }

    @Override
    public void triggerRestockRequest(Long id) {
        InventoryItem item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));

        try {
            Notification notif = new Notification(
                    "ROLE_PURCHASING_AGENT",
                    "Restocking Request Initiated: " + item.getPartName(),
                    "Storekeeper requested restocking for " + item.getPartName() + " (SKU: " + item.getSkuCode() 
                            + "). Current Stock: " + item.getQtyOnHand() + " units (Reorder Threshold: " + item.getReorderLevel() + ").",
                    "INFO",
                    "inventory"
            );
            notificationRepository.save(notif);
            logger.info("Restock request notification dispatched for item ID {}", id);
        } catch (Exception e) {
            logger.error("Failed to dispatch restock request notification for item ID {}: {}", id, e.getMessage(), e);
        }
    }

    @Override
    public void deleteItem(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Inventory item not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getApprovedItems() {
        return repository.findByApprovalStatusIgnoreCase("APPROVED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getPendingItems() {
        return repository.findByApprovalStatusIgnoreCase("PENDING");
    }

    @Override
    @Transactional
    public InventoryItem approveItem(Long id) {
        InventoryItem item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));
        item.setApprovalStatus("APPROVED");
        return repository.save(item);
    }

    @Override
    @Transactional
    public InventoryItem rejectItem(Long id) {
        InventoryItem item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));
        item.setApprovalStatus("REJECTED");
        return repository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return repository.count();
    }

    // Step 3: Spare part validation
    private void validateItem(InventoryItem item) {
        if (item.getPartName() == null || item.getPartName().trim().isEmpty()) {
            throw new IllegalArgumentException("Part name is required.");
        }
        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (item.getBrand() == null || item.getBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand is required.");
        }
        if (item.getSellingPrice() == null || item.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Selling price must be greater than 0 LKR.");
        }
        if (item.getUnitCost() == null || item.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit cost must be greater than 0 LKR.");
        }
        if (item.getQtyOnHand() == null || item.getQtyOnHand() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (item.getReorderLevel() == null || item.getReorderLevel() < 0) {
            throw new IllegalArgumentException("Reorder threshold cannot be negative.");
        }
    }

    // Step 7, 8 & 9: Low-Stock Threshold Monitoring & Failure Logging Exception Flow
    private void checkAndGenerateLowStockAlert(InventoryItem item) {
        if (item == null) return;
        try {
            if (item.getQtyOnHand() <= item.getReorderLevel()) {
                String notifTitle = "Low Stock Alert: " + item.getPartName();
                String notifMsg = "Stock level for " + item.getPartName() + " (" + item.getSkuCode() 
                        + ") has dropped to " + item.getQtyOnHand() + " units (Reorder Threshold: " + item.getReorderLevel() + ").";
                
                // Avoid flooding duplicate unread low-stock notifications for the same item
                Notification notif = new Notification(
                        "ROLE_STOREKEEPER",
                        notifTitle,
                        notifMsg,
                        "WARNING",
                        "inventory"
                );
                notificationRepository.save(notif);
                logger.info("Low-stock alert generated for item ID {}: {} (Qty: {})", item.getId(), item.getPartName(), item.getQtyOnHand());
            }
        } catch (Exception e) {
            // Step 9: Catch exception, log server-side, and notify Storekeeper of failure
            logger.error("ALERT GENERATION FAILED for item ID {}: {}", item.getId(), e.getMessage(), e);
            try {
                Notification failureNotif = new Notification(
                        "ROLE_STOREKEEPER",
                        "Alert Generation Failure",
                        "System encountered an error generating automated low-stock alert for " + item.getPartName() + ": " + e.getMessage(),
                        "DANGER",
                        "inventory"
                );
                notificationRepository.save(failureNotif);
            } catch (Exception ignored) {
                logger.error("Failed even to send alert failure notification to Storekeeper: {}", ignored.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.math.BigDecimal getTotalInventoryValue() {
        java.math.BigDecimal val = repository.getTotalInventoryValue();
        return val != null ? val : java.math.BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getApprovedCategoryCounts() {
        List<InventoryItem> approved = getApprovedItems();
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        counts.put("all", (long) approved.size());
        counts.put("cpu", 0L);
        counts.put("gpu", 0L);
        counts.put("ram", 0L);
        counts.put("motherboard", 0L);
        counts.put("psu", 0L);
        counts.put("storage", 0L);
        counts.put("cooling", 0L);
        counts.put("peripherals", 0L);

        for (InventoryItem item : approved) {
            String c = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            String key = "peripherals";
            if (c.contains("cpu") || c.contains("processor")) key = "cpu";
            else if (c.contains("gpu") || c.contains("graphics")) key = "gpu";
            else if (c.contains("ram") || c.contains("memory")) key = "ram";
            else if (c.contains("motherboard") || c.contains("board")) key = "motherboard";
            else if (c.contains("psu") || c.contains("power")) key = "psu";
            else if (c.contains("storage") || c.contains("ssd") || c.contains("hdd")) key = "storage";
            else if (c.contains("cooling") || c.contains("cooler") || c.contains("fan")) key = "cooling";

            counts.put(key, counts.get(key) + 1);
        }
        return counts;
    }

    private String generateUniqueSku(String category) {
        String prefix = (category != null && category.length() >= 3) 
                ? category.substring(0, 3).toUpperCase() 
                : "HW";
        int randomNum = 1000 + new Random().nextInt(9000);
        return "SKU-" + prefix + "-2026-" + randomNum;
    }
}
