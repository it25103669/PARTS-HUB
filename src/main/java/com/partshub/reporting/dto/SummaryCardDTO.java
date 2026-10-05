package com.partshub.reporting.dto;

/**
 * Represents a single KPI summary card displayed at the top of the dashboard.
 * Example: "Total Products – 1,247 (+5% from last month)"
 */
public class SummaryCardDTO {

    private String title;
    private String value;
    private String iconClass;        // Bootstrap Icons class, e.g. "bi-box-seam"
    private String colorClass;       // CSS class applied to the card accent, e.g. "accent-blue"
    private String changeLabel;      // e.g. "+5% from last month"
    private boolean changePositive;  // true = upward/good, false = downward/bad

    public SummaryCardDTO() {}

    public SummaryCardDTO(String title, String value, String iconClass,
                          String colorClass, String changeLabel, boolean changePositive) {
        this.title = title;
        this.value = value;
        this.iconClass = iconClass;
        this.colorClass = colorClass;
        this.changeLabel = changeLabel;
        this.changePositive = changePositive;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public String getColorClass() { return colorClass; }
    public void setColorClass(String colorClass) { this.colorClass = colorClass; }

    public String getChangeLabel() { return changeLabel; }
    public void setChangeLabel(String changeLabel) { this.changeLabel = changeLabel; }

    public boolean isChangePositive() { return changePositive; }
    public void setChangePositive(boolean changePositive) { this.changePositive = changePositive; }
}
