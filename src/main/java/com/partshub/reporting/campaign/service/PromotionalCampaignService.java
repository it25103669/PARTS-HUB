package com.partshub.reporting.campaign.service;

import com.partshub.reporting.campaign.dto.CampaignFormDTO;
import com.partshub.reporting.campaign.entity.PromotionalCampaign;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for Promotional Campaign Management.
 *
 * <p>All business logic (status resolution, CRUD operations) is
 * implemented in {@link PromotionalCampaignServiceImpl}.
 * Controllers must not contain business logic – delegate to this service.
 */
public interface PromotionalCampaignService {

    /**
     * Retrieve all campaigns, with their statuses refreshed against today's date.
     *
     * @return list of all campaigns (may be empty)
     */
    List<PromotionalCampaign> getAllCampaigns();

    /**
     * Find a campaign by its ID.
     *
     * @param id the campaign ID
     * @return Optional containing the campaign if found
     */
    Optional<PromotionalCampaign> getCampaignById(Long id);

    /**
     * Create a new campaign from the given form data.
     * Status is automatically determined from the dates.
     *
     * @param formDTO validated form data
     * @return the persisted campaign
     */
    PromotionalCampaign createCampaign(CampaignFormDTO formDTO);

    /**
     * Update an existing campaign. Status is re-resolved after update,
     * unless the campaign is already CANCELLED.
     *
     * @param id      the campaign ID to update
     * @param formDTO validated form data with new values
     * @return the updated and persisted campaign
     * @throws jakarta.persistence.EntityNotFoundException if campaign not found
     */
    PromotionalCampaign updateCampaign(Long id, CampaignFormDTO formDTO);

    /**
     * Mark a campaign as CANCELLED. Once cancelled, the status is
     * permanent – dates will not override it.
     *
     * @param id the campaign ID to cancel
     * @throws jakarta.persistence.EntityNotFoundException if campaign not found
     */
    void cancelCampaign(Long id);

    /**
     * Delete a campaign by ID.
     * Typically only used for CANCELLED or EXPIRED campaigns during the viva.
     *
     * @param id the campaign ID to delete
     * @throws jakarta.persistence.EntityNotFoundException if campaign not found
     */
    void deleteCampaign(Long id);

    /**
     * Count the number of campaigns currently in ACTIVE status.
     * Used by the Dashboard "Active Promotions" summary card.
     *
     * @return count of ACTIVE campaigns
     */
    long countActiveCampaigns();
}
