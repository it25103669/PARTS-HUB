package com.partshub.returns.service;

import com.partshub.returns.dto.SaleLookupDTO;
import com.partshub.returns.entity.ReturnClaim;
import java.util.List;
import java.util.Optional;

public interface ReturnsService {
    List<ReturnClaim> getAllClaims();
    List<ReturnClaim> searchAndFilter(String query, String type, String status);
    Optional<ReturnClaim> getClaimById(Long id);
    Optional<ReturnClaim> getClaimByNumber(String claimNumber);
    SaleLookupDTO lookupSale(String invoiceNumber, String customerName);
    ReturnClaim createClaim(ReturnClaim claim);
    ReturnClaim updateClaim(Long id, ReturnClaim details);
    ReturnClaim updateStatus(Long id, String status, String resolution);
    ReturnClaim resolveSupplierClaim(Long id, String supplierResolution, String notes);
    void deleteClaim(Long id);
    long countTotal();
}
