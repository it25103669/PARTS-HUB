package com.partshub.purchase.repository;

import com.partshub.purchase.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    @Query("SELECT p FROM PurchaseOrder p WHERE " +
           "(:query IS NULL OR LOWER(p.poNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.supplierName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR :status = 'ALL' OR p.status = :status) " +
           "ORDER BY p.orderDate DESC, p.id DESC")
    List<PurchaseOrder> searchAndFilter(@Param("query") String query, @Param("status") String status);

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE UPPER(p.status) IN ('DRAFT', 'PENDING', 'SENT')")
    long countOpenOrders();

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE UPPER(p.status) IN ('SENT', 'PENDING', 'PARTIALLY_RECEIVED', 'DISCREPANCY')")
    long countPendingReceipt();

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE UPPER(p.status) IN ('RECEIVED', 'COMPLETED') " +
           "AND FUNCTION('MONTH', p.orderDate) = FUNCTION('MONTH', CURRENT_DATE) " +
           "AND FUNCTION('YEAR', p.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    long countCompletedThisMonth();

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0.0) FROM PurchaseOrder p WHERE UPPER(p.status) <> 'CANCELLED'")
    double sumTotalPoValue();
}
