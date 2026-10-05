package com.partshub.sales.repository;

import com.partshub.sales.entity.SaleInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleInvoiceRepository extends JpaRepository<SaleInvoice, Long> {

    Optional<SaleInvoice> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT s FROM SaleInvoice s WHERE " +
           "(:query IS NULL OR LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.customerContact) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR :status = 'ALL' OR s.paymentStatus = :status) " +
           "ORDER BY s.saleDate DESC, s.id DESC")
    List<SaleInvoice> searchAndFilter(@Param("query") String query, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM SaleInvoice s WHERE s.paymentStatus = 'PAID'")
    BigDecimal calculateTotalPaidRevenue();
}
