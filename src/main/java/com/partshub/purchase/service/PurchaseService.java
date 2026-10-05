package com.partshub.purchase.service;

import com.partshub.purchase.entity.PurchaseOrder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PurchaseService {
    List<PurchaseOrder> getAllOrders();
    List<PurchaseOrder> searchAndFilter(String query, String status);
    Optional<PurchaseOrder> getOrderById(Long id);
    Optional<PurchaseOrder> getOrderByNumber(String poNumber);
    PurchaseOrder createOrder(PurchaseOrder order);
    PurchaseOrder updateOrder(Long id, PurchaseOrder details);
    void deleteOrder(Long id);
    PurchaseOrder cancelOrder(Long id);
    PurchaseOrder receiveOrder(Long id, List<Map<String, Object>> receivedItems, String notes);
    long countTotal();
    Map<String, Object> getPurchaseStats();
}
