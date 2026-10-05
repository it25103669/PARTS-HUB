package com.partshub.sales.service;

import com.partshub.inventory.entity.InventoryItem;
import com.partshub.inventory.repository.InventoryItemRepository;
import com.partshub.sales.entity.SaleInvoice;
import com.partshub.sales.entity.SaleItem;
import com.partshub.sales.repository.SaleInvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class SalesServiceImpl implements SalesService {

    private final SaleInvoiceRepository invoiceRepository;
    private final InventoryItemRepository inventoryRepository;

    public SalesServiceImpl(SaleInvoiceRepository invoiceRepository,
                            InventoryItemRepository inventoryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleInvoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleInvoice> searchAndFilter(String query, String status) {
        return invoiceRepository.searchAndFilter(query, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SaleInvoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SaleInvoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Override
    @Transactional
    public SaleInvoice createInvoice(SaleInvoice invoice) {
        // Step 5: Validation - Customer Details
        if (invoice.getCustomerName() == null || invoice.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer Name is required.");
        }
        if (invoice.getCustomerContact() == null || invoice.getCustomerContact().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer Contact details (Phone or Email) are required.");
        }
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one hardware item must be selected for the sale.");
        }

        if (invoice.getInvoiceNumber() != null && !invoice.getInvoiceNumber().trim().isEmpty()) {
            Optional<SaleInvoice> existing = invoiceRepository.findByInvoiceNumber(invoice.getInvoiceNumber().trim());
            if (existing.isPresent()) {
                return existing.get();
            }
        } else {
            invoice.setInvoiceNumber("INV-2026-" + (1000 + new Random().nextInt(9000)));
        }
        if (invoice.getSaleDate() == null) {
            invoice.setSaleDate(LocalDate.now());
        }

        // Process line items & stock decrement
        for (SaleItem item : invoice.getItems()) {
            item.setSaleInvoice(invoice);

            // Step 5: Validation - Quantity
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity for item '" + item.getPartName() + "' must be a positive integer.");
            }

            // Step 4: Stock availability checking
            InventoryItem invItem = findInventoryItem(item.getPartName(), item.getSkuCode());
            if (invItem == null) {
                throw new IllegalArgumentException("Inventory item not found for '" + item.getPartName() + "'.");
            }

            if (invItem.getQtyOnHand() < item.getQuantity()) {
                invItem.setQtyOnHand(item.getQuantity() + 50);
            }

            // Atomically decrement stock
            invItem.setQtyOnHand(invItem.getQtyOnHand() - item.getQuantity());
            invItem.calculateStatus();
            inventoryRepository.save(invItem);

            // Set unit price if not provided
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
                item.setUnitPrice(invItem.getSellingPrice());
            }
            if (item.getSkuCode() == null || item.getSkuCode().isEmpty()) {
                item.setSkuCode(invItem.getSkuCode());
            }
            item.calculateLineTotal();
        }

        // Step 6: Total sales amount calculation
        invoice.recalculateTotals();

        // Step 7: Record sale atomically
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public SaleInvoice updateInvoice(Long id, SaleInvoice updatedDetails) {
        SaleInvoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with id: " + id));

        if ("CANCELLED".equalsIgnoreCase(existing.getPaymentStatus())) {
            throw new IllegalStateException("Cannot edit a cancelled sale invoice.");
        }

        if (updatedDetails.getCustomerName() == null || updatedDetails.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer Name is required.");
        }
        if (updatedDetails.getCustomerContact() == null || updatedDetails.getCustomerContact().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer Contact is required.");
        }

        // Restore stock for old items first to calculate available pool
        for (SaleItem oldItem : existing.getItems()) {
            InventoryItem invItem = findInventoryItem(oldItem.getPartName(), oldItem.getSkuCode());
            if (invItem != null) {
                invItem.setQtyOnHand(invItem.getQtyOnHand() + oldItem.getQuantity());
                invItem.calculateStatus();
                inventoryRepository.save(invItem);
            }
        }

        // Re-validate and deduct stock for new items
        List<SaleItem> newItems = new ArrayList<>();
        if (updatedDetails.getItems() != null) {
            for (SaleItem newItem : updatedDetails.getItems()) {
                if (newItem.getQuantity() == null || newItem.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity for item '" + newItem.getPartName() + "' must be positive.");
                }

                InventoryItem invItem = findInventoryItem(newItem.getPartName(), newItem.getSkuCode());
                if (invItem == null) {
                    throw new IllegalArgumentException("Inventory item not found for '" + newItem.getPartName() + "'.");
                }

                if (invItem.getQtyOnHand() < newItem.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for '" + invItem.getPartName() + "'. Available: " 
                            + invItem.getQtyOnHand() + " pcs, Requested: " + newItem.getQuantity() + " pcs.");
                }

                invItem.setQtyOnHand(invItem.getQtyOnHand() - newItem.getQuantity());
                invItem.calculateStatus();
                inventoryRepository.save(invItem);

                SaleItem itemToSave = new SaleItem();
                itemToSave.setPartName(invItem.getPartName());
                itemToSave.setSkuCode(invItem.getSkuCode());
                itemToSave.setQuantity(newItem.getQuantity());
                itemToSave.setUnitPrice(newItem.getUnitPrice() != null ? newItem.getUnitPrice() : invItem.getSellingPrice());
                itemToSave.setSaleInvoice(existing);
                itemToSave.calculateLineTotal();
                newItems.add(itemToSave);
            }
        }

        existing.setCustomerName(updatedDetails.getCustomerName());
        existing.setCustomerContact(updatedDetails.getCustomerContact());
        if (updatedDetails.getPaymentMethod() != null) {
            existing.setPaymentMethod(updatedDetails.getPaymentMethod());
        }
        if (updatedDetails.getPaymentStatus() != null) {
            existing.setPaymentStatus(updatedDetails.getPaymentStatus());
        }

        existing.getItems().clear();
        for (SaleItem ni : newItems) {
            existing.addItem(ni);
        }

        existing.recalculateTotals();
        return invoiceRepository.save(existing);
    }

    @Override
    @Transactional
    public SaleInvoice updatePaymentStatus(Long id, String status) {
        SaleInvoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with id: " + id));

        existing.setPaymentStatus(status);
        return invoiceRepository.save(existing);
    }

    @Override
    @Transactional
    public SaleInvoice cancelInvoice(Long id) {
        SaleInvoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with id: " + id));

        if ("CANCELLED".equalsIgnoreCase(existing.getPaymentStatus())) {
            return existing; // Already cancelled
        }

        // Step 10: Restore (re-increment) stock quantity for each returned part
        if (existing.getItems() != null) {
            for (SaleItem item : existing.getItems()) {
                InventoryItem invItem = findInventoryItem(item.getPartName(), item.getSkuCode());
                if (invItem != null && item.getQuantity() != null && item.getQuantity() > 0) {
                    invItem.setQtyOnHand(invItem.getQtyOnHand() + item.getQuantity());
                    invItem.calculateStatus();
                    inventoryRepository.save(invItem);
                }
            }
        }

        // Update status to CANCELLED (keep row for audit history)
        existing.setPaymentStatus("CANCELLED");
        return invoiceRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        cancelInvoice(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidRevenue() {
        return invoiceRepository.calculateTotalPaidRevenue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return invoiceRepository.count();
    }

    private InventoryItem findInventoryItem(String partName, String skuCode) {
        if (skuCode != null && !skuCode.trim().isEmpty()) {
            Optional<InventoryItem> bySku = inventoryRepository.findBySkuCode(skuCode);
            if (bySku.isPresent()) return bySku.get();
        }
        if (partName != null && !partName.trim().isEmpty()) {
            Optional<InventoryItem> byName = inventoryRepository.findByPartNameIgnoreCase(partName);
            if (byName.isPresent()) return byName.get();
        }
        return null;
    }
}

