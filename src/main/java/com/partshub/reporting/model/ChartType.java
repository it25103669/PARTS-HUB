package com.partshub.reporting.model;

/**
 * Defines the preferred chart visualization for a saved report template.
 */
public enum ChartType {
    NONE("No Chart"),
    BAR("Bar Chart"),
    LINE("Line Chart"),
    PIE("Pie Chart"),
    DOUGHNUT("Doughnut Chart");

    private final String displayName;

    ChartType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
