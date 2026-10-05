package com.partshub.inventory.service;

import com.partshub.inventory.entity.InventoryItem;
import java.util.List;
import java.util.Optional;

public interface InventoryService {
    List<InventoryItem> getAllItems();
    List<InventoryItem> searchAndFilter(String query, String category, String status, String sort);
    List<InventoryItem> searchAndFilter(String query, String category, String brand, String status, String sort);
    Optional<InventoryItem> getItemById(Long id);
    Optional<InventoryItem> getItemBySku(String skuCode);
    InventoryItem createItem(InventoryItem item);
    InventoryItem updateItem(Long id, InventoryItem itemDetails);
    InventoryItem updateStock(Long id, int delta);
    void deleteItem(Long id);
    void adjustStock(String partNameOrSku, int delta);
    void triggerRestockRequest(Long id);
    List<InventoryItem> getApprovedItems();
    List<InventoryItem> getPendingItems();
    InventoryItem approveItem(Long id);
    InventoryItem rejectItem(Long id);
    long countTotal();
    java.math.BigDecimal getTotalInventoryValue();
    java.util.Map<String, Long> getApprovedCategoryCounts();
}
