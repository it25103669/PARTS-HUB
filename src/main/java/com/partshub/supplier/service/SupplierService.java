package com.partshub.supplier.service;

import com.partshub.supplier.entity.Supplier;
import com.partshub.supplier.entity.SupplierAction;
import com.partshub.supplier.entity.SupplierPart;
import java.util.List;
import java.util.Optional;

public interface SupplierService {
    List<Supplier> getAllSuppliers();
    List<Supplier> getActiveSuppliers();
    List<Supplier> searchAndRank(String query, String status, String sort);
    Optional<Supplier> getSupplierById(Long id);
    Optional<Supplier> getSupplierByCode(String supplierCode);
    Supplier createSupplier(Supplier supplier);
    Supplier updateSupplier(Long id, Supplier details);
    void deleteSupplier(Long id);
    Supplier toggleStatus(Long id);
    Supplier deactivateSupplier(Long id);
    Supplier activateSupplier(Long id);
    Supplier recordAction(Long supplierId, SupplierAction action);
    List<SupplierAction> getSupplierActions(Long supplierId);
    long countTotal();

    // Linked Spare Parts & Pricing/Delivery Comparison
    List<SupplierPart> getPartsBySupplier(Long supplierId);
    SupplierPart linkPartToSupplier(Long supplierId, SupplierPart part);
    void unlinkPartFromSupplier(Long supplierId, Long partId);
    List<SupplierPart> getPricingComparison(String category);
}
