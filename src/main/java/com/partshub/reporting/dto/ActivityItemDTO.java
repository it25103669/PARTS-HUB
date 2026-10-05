package com.partshub.reporting.dto;

/**
 * Represents a single entry in the "Recent Business Activity" feed on the dashboard.
 */
public class ActivityItemDTO {

    private String description;   // e.g. "Sale #INV-2024-0891 completed"
    private String timeAgo;       // e.g. "2 hours ago"
    private String iconClass;     // Bootstrap Icons class
    private String badgeClass;    // CSS class for the coloured dot, e.g. "badge-success"

    public ActivityItemDTO() {}

    public ActivityItemDTO(String description, String timeAgo,
                           String iconClass, String badgeClass) {
        this.description = description;
        this.timeAgo = timeAgo;
        this.iconClass = iconClass;
        this.badgeClass = badgeClass;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public String getBadgeClass() { return badgeClass; }
    public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }
}
