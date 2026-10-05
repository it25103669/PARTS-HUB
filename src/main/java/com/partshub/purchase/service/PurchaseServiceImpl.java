package com.partshub.purchase.service;

import com.partshub.inventory.service.InventoryService;
import com.partshub.purchase.entity.PurchaseItem;
import com.partshub.purchase.entity.PurchaseOrder;
import com.partshub.purchase.repository.PurchaseOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryService inventoryService;

    public PurchaseServiceImpl(PurchaseOrderRepository purchaseOrderRepository, InventoryService inventoryService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrder> searchAndFilter(String query, String status) {
        return purchaseOrderRepository.searchAndFilter(query, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrder> getOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrder> getOrderByNumber(String poNumber) {
        return purchaseOrderRepository.findByPoNumber(poNumber);
    }

    @Override
    public PurchaseOrder createOrder(PurchaseOrder order) {
        if (order.getSupplierName() == null || order.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier selection is required.");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one line item is required for a purchase order.");
        }

        if (order.getPoNumber() == null || order.getPoNumber().trim().isEmpty()) {
            order.setPoNumber("PO-2026-" + (8800 + new Random().nextInt(1000)));
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        if (order.getExpectedDeliveryDate() == null) {
            order.setExpectedDeliveryDate(order.getOrderDate().plusDays(7));
        }
        if (order.getStatus() == null || order.getStatus().trim().isEmpty()) {
            order.setStatus("SENT");
        }

        for (PurchaseItem item : order.getItems()) {
            if (item.getPartName() == null || item.getPartName().trim().isEmpty()) {
                throw new IllegalArgumentException("Line item part name cannot be empty.");
            }
            if (item.getQtyOrdered() == null || item.getQtyOrdered() <= 0) {
                throw new IllegalArgumentException("Line item quantities must be positive integers.");
            }
            item.setPurchaseOrder(order);
            item.calculateLineTotal();
        }
        order.recalculateTotal();

        return purchaseOrderRepository.save(order);
    }

    @Override
    public PurchaseOrder updateOrder(Long id, PurchaseOrder details) {
        PurchaseOrder existing = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with id: " + id));

        String status = existing.getStatus() != null ? existing.getStatus().toUpperCase() : "";
        if ("SENT".equals(status) || "RECEIVED".equals(status) || "PARTIALLY_RECEIVED".equals(status) || "COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            throw new IllegalStateException("Cannot edit a Purchase Order with status '" + existing.getStatus() + "'. Only DRAFT or PENDING orders can be updated.");
        }

        if (details.getSupplierName() != null && !details.getSupplierName().trim().isEmpty()) {
            existing.setSupplierName(details.getSupplierName());
        }
        if (details.getOrderDate() != null) {
            existing.setOrderDate(details.getOrderDate());
        }
        if (details.getExpectedDeliveryDate() != null) {
            existing.setExpectedDeliveryDate(details.getExpectedDeliveryDate());
        }
        if (details.getStatus() != null && !details.getStatus().trim().isEmpty()) {
            existing.setStatus(details.getStatus());
        }
        if (details.getNotes() != null) {
            existing.setNotes(details.getNotes());
        }

        // If items are provided in update payload
        if (details.getItems() != null && !details.getItems().isEmpty()) {
            existing.getItems().clear();
            for (PurchaseItem item : details.getItems()) {
                if (item.getQtyOrdered() != null && item.getQtyOrdered() > 0) {
                    item.setPurchaseOrder(existing);
                    item.calculateLineTotal();
                    existing.getItems().add(item);
                }
            }
            existing.recalculateTotal();
        }

        return purchaseOrderRepository.save(existing);
    }

    @Override
    public void deleteOrder(Long id) {
        PurchaseOrder existing = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with id: " + id));

        // Deletion rule: Only DRAFT purchase orders can be deleted
        String status = existing.getStatus() != null ? existing.getStatus().toUpperCase() : "";
        if (!"DRAFT".equals(status)) {
            throw new IllegalStateException("Cannot delete Purchase Order " + existing.getPoNumber() + " with status '" + existing.getStatus() + "'. Deletion is restricted to DRAFT purchase orders only. For sent orders, please use Cancel.");
        }

        purchaseOrderRepository.deleteById(id);
    }

    @Override
    public PurchaseOrder cancelOrder(Long id) {
        PurchaseOrder existing = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with id: " + id));

        String status = existing.getStatus() != null ? existing.getStatus().toUpperCase() : "";
        if ("RECEIVED".equals(status) || "COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            throw new IllegalStateException("Cannot cancel a Purchase Order with status '" + existing.getStatus() + "'.");
        }

        existing.setStatus("CANCELLED");
        String note = "[ORDER CANCELLED on " + LocalDate.now() + "]";
        existing.setNotes(existing.getNotes() != null && !existing.getNotes().isEmpty() ? existing.getNotes() + " | " + note : note);
        return purchaseOrderRepository.save(existing);
    }

    @Override
    public PurchaseOrder receiveOrder(Long id, List<Map<String, Object>> receivedItems, String notes) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with id: " + id));

        boolean hasDiscrepancy = false;
        StringBuilder discrepancyDetails = new StringBuilder();

        if (order.getItems() != null) {
            for (PurchaseItem item : order.getItems()) {
                int receivedQty = item.getQtyOrdered();
                String condition = "GOOD";

                if (receivedItems != null) {
                    for (Map<String, Object> map : receivedItems) {
                        String name = (String) map.get("partName");
                        if (name != null && name.equalsIgnoreCase(item.getPartName())) {
                            if (map.get("qtyReceived") != null) {
                                receivedQty = Integer.parseInt(map.get("qtyReceived").toString());
                            }
                            if (map.get("condition") != null) {
                                condition = map.get("condition").toString();
                            }
                            break;
                        }
                    }
                }

                item.setQtyReceived(receivedQty);
                item.setConditionStatus(condition);

                // Discrepancy checks: receivedQty < orderedQty or condition is not GOOD
                if (receivedQty < item.getQtyOrdered() || !"GOOD".equalsIgnoreCase(condition)) {
                    hasDiscrepancy = true;
                    if (discrepancyDetails.length() > 0) discrepancyDetails.append("; ");
                    discrepancyDetails.append(item.getPartName()).append(": Ordered ").append(item.getQtyOrdered())
                            .append(", Received ").append(receivedQty).append(" (").append(condition).append(")");
                }

                // If units are in GOOD condition, credit them to inventory stock
                if ("GOOD".equalsIgnoreCase(condition) && receivedQty > 0) {
                    inventoryService.adjustStock(item.getPartName(), receivedQty);
                }
            }
        }

        if (hasDiscrepancy) {
            order.setStatus("DISCREPANCY");
            String logMsg = "[DISCREPANCY DETECTED: " + discrepancyDetails.toString() + "]";
            String existingNotes = order.getNotes() != null ? order.getNotes() + " | " : "";
            order.setNotes(existingNotes + logMsg);
        } else {
            order.setStatus("RECEIVED");
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = order.getNotes() != null ? order.getNotes() + " | " : "";
                order.setNotes(existingNotes + "[Received Note: " + notes + "]");
            }
        }

        return purchaseOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return purchaseOrderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPurchaseStats() {
        long openCount = purchaseOrderRepository.countOpenOrders();
        long pendingReceiptCount = purchaseOrderRepository.countPendingReceipt();
        long completedThisMonthCount = purchaseOrderRepository.countCompletedThisMonth();
        double totalValue = purchaseOrderRepository.sumTotalPoValue();

        return Map.of(
                "openOrders", openCount,
                "pendingReceipt", pendingReceiptCount,
                "completedThisMonth", completedThisMonthCount,
                "totalPoValue", totalValue
        );
    }
}
