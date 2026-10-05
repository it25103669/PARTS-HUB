package com.partshub.supplier.repository;

import com.partshub.supplier.entity.SupplierAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SupplierActionRepository extends JpaRepository<SupplierAction, Long> {
    List<SupplierAction> findBySupplierIdOrderByActionDateDesc(Long supplierId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SupplierAction a WHERE a.supplierId = :supplierId")
    void deleteBySupplierId(@Param("supplierId") Long supplierId);
}
