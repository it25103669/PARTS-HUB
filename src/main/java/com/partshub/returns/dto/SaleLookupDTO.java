package com.partshub.returns.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleLookupDTO {
    private String invoiceNumber;
    private String customerName;
    private String customerContact;
    private LocalDate saleDate;
    private List<SaleItemLookupDTO> items = new ArrayList<>();

    public SaleLookupDTO() {}

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerContact() { return customerContact; }
    public void setCustomerContact(String customerContact) { this.customerContact = customerContact; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public List<SaleItemLookupDTO> getItems() { return items; }
    public void setItems(List<SaleItemLookupDTO> items) { this.items = items; }
}
