package com.partshub.supplier.service;

import com.partshub.supplier.entity.Supplier;
import com.partshub.supplier.entity.SupplierAction;
import com.partshub.supplier.entity.SupplierPart;
import com.partshub.supplier.repository.SupplierActionRepository;
import com.partshub.supplier.repository.SupplierPartRepository;
import com.partshub.supplier.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierActionRepository actionRepository;
    private final SupplierPartRepository partRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository,
                               SupplierActionRepository actionRepository,
                               SupplierPartRepository partRepository) {
        this.supplierRepository = supplierRepository;
        this.actionRepository = actionRepository;
        this.partRepository = partRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByIsActiveTrueOrderByScoreDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> searchAndRank(String query, String status, String sort) {
        List<Supplier> list = supplierRepository.searchAndRank(query, status);

        if (sort != null) {
            switch (sort) {
                case "score-asc":
                    list.sort(Comparator.comparing(Supplier::getScore));
                    break;
                case "orders-desc":
                    list.sort(Comparator.comparing(Supplier::getOrdersCompleted).reversed());
                    break;
                case "name-asc":
                    list.sort(Comparator.comparing(Supplier::getSupplierName, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "lead-time-asc":
                    list.sort(Comparator.comparing(s -> s.getLeadTimeDays() != null ? s.getLeadTimeDays() : 999));
                    break;
                case "on-time-desc":
                    list.sort(Comparator.comparing(Supplier::getOnTimeRate).reversed());
                    break;
                case "score-desc":
                default:
                    list.sort(Comparator.comparing(Supplier::getScore).reversed());
            }
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> getSupplierByCode(String supplierCode) {
        return supplierRepository.findBySupplierCode(supplierCode);
    }

    @Override
    public Supplier createSupplier(Supplier supplier) {
        // 1. Generate code if blank
        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().trim().isEmpty()) {
            supplier.setSupplierCode("SUP-LK-" + String.format("%04d", 100 + new Random().nextInt(900)));
        } else {
            supplier.setSupplierCode(supplier.getSupplierCode().trim());
        }

        // 2. Prevent duplicate registrations (Code, Name, Email)
        if (supplierRepository.existsBySupplierCode(supplier.getSupplierCode())) {
            throw new IllegalArgumentException("Supplier with registration code '" + supplier.getSupplierCode() + "' already exists.");
        }
        if (supplier.getSupplierName() != null && supplierRepository.existsBySupplierNameIgnoreCase(supplier.getSupplierName().trim())) {
            throw new IllegalArgumentException("Supplier with name '" + supplier.getSupplierName().trim() + "' is already registered.");
        }
        if (supplier.getContactEmail() != null && !supplier.getContactEmail().trim().isEmpty()
                && supplierRepository.existsByContactEmailIgnoreCase(supplier.getContactEmail().trim())) {
            throw new IllegalArgumentException("Contact email '" + supplier.getContactEmail().trim() + "' is already associated with an existing supplier.");
        }

        if (supplier.getAddress() == null || supplier.getAddress().trim().isEmpty()) {
            supplier.setAddress("Colombo, Western Province, Sri Lanka");
        }
        if (supplier.getLeadTimeDays() == null) {
            supplier.setLeadTimeDays(5);
        }

        supplier.setIsActive(true);
        supplier.setStatus("ACTIVE");
        supplier.recalculateTier();
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier updateSupplier(Long id, Supplier details) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + id));

        String newName = details.getSupplierName() != null ? details.getSupplierName().trim() : existing.getSupplierName();
        String newCode = details.getSupplierCode() != null ? details.getSupplierCode().trim() : existing.getSupplierCode();
        String newEmail = details.getContactEmail() != null ? details.getContactEmail().trim() : existing.getContactEmail();

        // Check duplicates excluding this supplier's ID
        if (newCode != null && supplierRepository.existsBySupplierCodeAndIdNot(newCode, id)) {
            throw new IllegalArgumentException("Supplier code '" + newCode + "' is already in use by another supplier.");
        }
        if (newName != null && supplierRepository.existsBySupplierNameIgnoreCaseAndIdNot(newName, id)) {
            throw new IllegalArgumentException("Supplier name '" + newName + "' is already registered to another supplier.");
        }
        if (newEmail != null && !newEmail.isEmpty() && supplierRepository.existsByContactEmailIgnoreCaseAndIdNot(newEmail, id)) {
            throw new IllegalArgumentException("Contact email '" + newEmail + "' is already in use by another supplier.");
        }

        existing.setSupplierName(newName);
        existing.setSupplierCode(newCode);
        if (details.getCategory() != null) existing.setCategory(details.getCategory());
        if (details.getContactPhone() != null) existing.setContactPhone(details.getContactPhone());
        existing.setContactEmail(newEmail);
        if (details.getAddress() != null) existing.setAddress(details.getAddress());
        if (details.getLeadTimeDays() != null) existing.setLeadTimeDays(details.getLeadTimeDays());
        if (details.getOrdersCompleted() != null) existing.setOrdersCompleted(details.getOrdersCompleted());
        if (details.getScore() != null) existing.setScore(details.getScore());
        if (details.getStatus() != null) existing.setStatus(details.getStatus());
        if (details.getIsActive() != null) existing.setIsActive(details.getIsActive());

        existing.recalculateTier();
        return supplierRepository.save(existing);
    }

    @Override
    public Supplier toggleStatus(Long id) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + id));
        boolean currentActive = existing.getIsActive();
        existing.setIsActive(!currentActive);
        return supplierRepository.save(existing);
    }

    @Override
    public Supplier deactivateSupplier(Long id) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + id));
        existing.setIsActive(false);
        return supplierRepository.save(existing);
    }

    @Override
    public Supplier activateSupplier(Long id) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + id));
        existing.setIsActive(true);
        return supplierRepository.save(existing);
    }

    @Override
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new EntityNotFoundException("Supplier not found with id: " + id);
        }
        partRepository.deleteBySupplierId(id);
        actionRepository.deleteBySupplierId(id);
        supplierRepository.deleteById(id);
    }

    @Override
    public Supplier recordAction(Long supplierId, SupplierAction action) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + supplierId));

        action.setSupplierId(supplierId);
        actionRepository.save(action);

        // Recompute dynamic score
        int currentScore = supplier.getScore();
        int newScore = Math.max(0, Math.min(100, currentScore + action.getScoreImpact()));
        supplier.setScore(newScore);

        return supplierRepository.save(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierAction> getSupplierActions(Long supplierId) {
        return actionRepository.findBySupplierIdOrderByActionDateDesc(supplierId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return supplierRepository.count();
    }

    // --- Linked Spare Parts Implementations ---

    @Override
    @Transactional(readOnly = true)
    public List<SupplierPart> getPartsBySupplier(Long supplierId) {
        return partRepository.findBySupplierIdOrderByPartNameAsc(supplierId);
    }

    @Override
    public SupplierPart linkPartToSupplier(Long supplierId, SupplierPart part) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + supplierId));

        part.setSupplierId(supplierId);
        part.setSupplierName(supplier.getSupplierName());
        if (part.getCategory() == null || part.getCategory().isBlank()) {
            part.setCategory(supplier.getCategory());
        }
        if (part.getLeadTimeDays() == null) {
            part.setLeadTimeDays(supplier.getLeadTimeDays() != null ? supplier.getLeadTimeDays() : 5);
        }

        // If duplicate part for same supplier, update price/lead time
        Optional<SupplierPart> existing = partRepository.findBySupplierIdAndSkuCode(supplierId, part.getSkuCode());
        if (existing.isPresent()) {
            SupplierPart toUpdate = existing.get();
            toUpdate.setSupplyPrice(part.getSupplyPrice());
            toUpdate.setLeadTimeDays(part.getLeadTimeDays());
            toUpdate.setPartName(part.getPartName());
            toUpdate.setCategory(part.getCategory());
            toUpdate.setAvailabilityStatus("AVAILABLE");
            return partRepository.save(toUpdate);
        }

        return partRepository.save(part);
    }

    @Override
    public void unlinkPartFromSupplier(Long supplierId, Long partId) {
        partRepository.deleteBySupplierIdAndId(supplierId, partId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierPart> getPricingComparison(String category) {
        return partRepository.findForComparison(category);
    }
}
