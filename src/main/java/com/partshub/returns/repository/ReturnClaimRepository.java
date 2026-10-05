package com.partshub.returns.repository;

import com.partshub.returns.entity.ReturnClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnClaimRepository extends JpaRepository<ReturnClaim, Long> {

    Optional<ReturnClaim> findByClaimNumber(String claimNumber);

    @Query("SELECT r FROM ReturnClaim r WHERE " +
           "(:query IS NULL OR LOWER(r.claimNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(r.partName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(r.referenceNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (r.partyName IS NOT NULL AND LOWER(r.partyName) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "AND (:type IS NULL OR :type = 'ALL' OR r.claimType = :type) " +
           "AND (:status IS NULL OR :status = 'ALL' OR r.status = :status) " +
           "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReturnClaim> searchAndFilter(@Param("query") String query,
                                      @Param("type") String type,
                                      @Param("status") String status);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM ReturnClaim r WHERE LOWER(r.referenceNumber) = LOWER(:ref) AND LOWER(r.partName) = LOWER(:partName) AND r.status != 'REJECTED'")
    Integer countReturnedQtyByRefAndPart(@Param("ref") String ref, @Param("partName") String partName);
}
