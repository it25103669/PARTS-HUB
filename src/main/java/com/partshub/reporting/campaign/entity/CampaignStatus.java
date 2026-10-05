package com.partshub.reporting.campaign.entity;

/**
 * Represents the lifecycle status of a Promotional Campaign.
 *
 * <p>Status transitions:
 * <ul>
 *   <li>UPCOMING  – campaign start date is in the future</li>
 *   <li>ACTIVE    – today is within [startDate, endDate]</li>
 *   <li>EXPIRED   – campaign end date has passed</li>
 *   <li>CANCELLED – manually cancelled by a manager; immutable once set</li>
 * </ul>
 *
 * <p>A CANCELLED campaign must remain CANCELLED regardless of its dates.
 * Status is resolved automatically by {@code PromotionalCampaignServiceImpl#resolveStatus}.
 */
public enum CampaignStatus {
    UPCOMING,
    ACTIVE,
    EXPIRED,
    CANCELLED
}
