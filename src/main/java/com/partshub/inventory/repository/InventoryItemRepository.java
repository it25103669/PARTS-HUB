package com.partshub.inventory.repository;

import com.partshub.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findBySkuCode(String skuCode);

    Optional<InventoryItem> findByPartNameIgnoreCase(String partName);

    List<InventoryItem> findByPartNameContainingIgnoreCase(String partName);

    boolean existsByPartNameIgnoreCaseAndBrandIgnoreCase(String partName, String brand);

    boolean existsBySkuCodeIgnoreCase(String skuCode);

    List<InventoryItem> findByCategory(String category);

    List<InventoryItem> findByStatus(String status);

    List<InventoryItem> findByApprovalStatusIgnoreCase(String approvalStatus);

    List<InventoryItem> findByApprovalStatus(String approvalStatus);

    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(:query IS NULL OR LOWER(i.partName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(i.skuCode) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(i.brand) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(i.compatibility) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:category IS NULL OR :category = 'ALL' OR i.category = :category) " +
           "AND (:brand IS NULL OR :brand = 'ALL' OR LOWER(i.brand) = LOWER(:brand)) " +
           "AND (:status IS NULL OR :status = 'ALL' OR i.status = :status)")
    List<InventoryItem> searchAndFilter(@Param("query") String query,
                                       @Param("category") String category,
                                       @Param("brand") String brand,
                                       @Param("status") String status);

    @Query("SELECT COALESCE(SUM(i.qtyOnHand * i.unitCost), 0) FROM InventoryItem i")
    java.math.BigDecimal getTotalInventoryValue();

    @Query("SELECT i.category, COUNT(i) FROM InventoryItem i WHERE UPPER(i.approvalStatus) = 'APPROVED' GROUP BY i.category")
    List<Object[]> countApprovedItemsGroupedByCategory();

    default List<InventoryItem> searchAndFilter(String query, String category, String status) {
        return searchAndFilter(query, category, null, status);
    }
}
