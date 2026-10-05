package com.partshub.sales.service;

import com.partshub.sales.entity.SaleInvoice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SalesService {
    List<SaleInvoice> getAllInvoices();
    List<SaleInvoice> searchAndFilter(String query, String status);
    Optional<SaleInvoice> getInvoiceById(Long id);
    Optional<SaleInvoice> getInvoiceByNumber(String invoiceNumber);
    SaleInvoice createInvoice(SaleInvoice invoice);
    SaleInvoice updateInvoice(Long id, SaleInvoice updatedDetails);
    SaleInvoice updatePaymentStatus(Long id, String status);
    SaleInvoice cancelInvoice(Long id);
    void deleteInvoice(Long id);
    BigDecimal getTotalPaidRevenue();
    long countTotal();
}
