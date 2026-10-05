package com.partshub.auth.entity;

/**
 * Stakeholder Roles in PartsHub ERP System.
 * 
 * Each role owns a single operational module for full CRUD operations,
 * while maintaining read-only view access across all 6 modules.
 * Business Owner has full unrestricted CRUD access across all modules.
 */
public enum Role {
    STOREKEEPER("Inventory Management Officer", "inventory"),
    PROCUREMENT_OFFICER("Supplier Management Officer", "suppliers"),
    PURCHASING_AGENT("Purchase & Receiving Manager", "purchases"),
    SALES_REP("Sales & Billing Representative", "sales"),
    RETURNS_CLERK("Returns & Warranty Officer", "returns"),
    BUSINESS_OWNER("Executive Business Owner", "all");

    private final String displayTitle;
    private final String ownedModule;

    Role(String displayTitle, String ownedModule) {
        this.displayTitle = displayTitle;
        this.ownedModule = ownedModule;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getOwnedModule() {
        return ownedModule;
    }
}
