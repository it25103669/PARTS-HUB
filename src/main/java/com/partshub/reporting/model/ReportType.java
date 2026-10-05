package com.partshub.reporting.model;

/**
 * Enum representing the available report types in the system.
 * Used by ReportFilterDTO and ReportService to determine which
 * data set and column layout to generate.
 */
public enum ReportType {

    INVENTORY("Inventory Report"),
    SALES("Sales Report"),
    PURCHASE("Purchase Report"),
    LOW_STOCK("Low-Stock Report"),
    SUPPLIER_PERFORMANCE("Supplier Performance Report"),
    PRODUCT_PERFORMANCE("Product Performance Report"),
    GROSS_PROFIT("Estimated Gross Profit Report");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
