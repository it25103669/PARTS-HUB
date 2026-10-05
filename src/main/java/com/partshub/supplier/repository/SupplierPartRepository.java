package com.partshub.supplier.repository;

import com.partshub.supplier.entity.SupplierPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierPartRepository extends JpaRepository<SupplierPart, Long> {

    List<SupplierPart> findBySupplierIdOrderByPartNameAsc(Long supplierId);

    List<SupplierPart> findBySkuCode(String skuCode);

    Optional<SupplierPart> findBySupplierIdAndSkuCode(Long supplierId, String skuCode);

    @Modifying
    @Transactional
    @Query("DELETE FROM SupplierPart sp WHERE sp.supplierId = :supplierId")
    void deleteBySupplierId(@Param("supplierId") Long supplierId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SupplierPart sp WHERE sp.supplierId = :supplierId AND sp.id = :id")
    void deleteBySupplierIdAndId(@Param("supplierId") Long supplierId, @Param("id") Long id);

    @Query("SELECT sp FROM SupplierPart sp WHERE " +
           "(:category IS NULL OR :category = 'ALL' OR LOWER(sp.category) = LOWER(:category)) " +
           "ORDER BY sp.skuCode ASC, sp.supplyPrice ASC")
    List<SupplierPart> findForComparison(@Param("category") String category);
}
