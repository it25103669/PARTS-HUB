package com.partshub.reporting.model;

/**
 * Defines the date range applied to a report filter or saved template.
 */
public enum DateRangeType {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom Date Range"),
    ALL_TIME("All Time");

    private final String displayName;

    DateRangeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
