package com.partshub.supplier.repository;

import com.partshub.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findBySupplierCode(String supplierCode);

    Optional<Supplier> findBySupplierNameIgnoreCase(String supplierName);

    Optional<Supplier> findByContactEmailIgnoreCase(String contactEmail);

    boolean existsBySupplierCode(String supplierCode);

    boolean existsBySupplierNameIgnoreCase(String supplierName);

    boolean existsByContactEmailIgnoreCase(String contactEmail);

    boolean existsBySupplierCodeAndIdNot(String supplierCode, Long id);

    boolean existsBySupplierNameIgnoreCaseAndIdNot(String supplierName, Long id);

    boolean existsByContactEmailIgnoreCaseAndIdNot(String contactEmail, Long id);

    List<Supplier> findByIsActiveTrueOrderByScoreDesc();

    @Query("SELECT s FROM Supplier s WHERE " +
           "(:query IS NULL OR LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.contactEmail) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR :status = 'ALL' OR s.status = :status) " +
           "ORDER BY s.score DESC")
    List<Supplier> searchAndRank(@Param("query") String query, @Param("status") String status);
}
