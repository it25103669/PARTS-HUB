package com.partshub.reporting.campaign.service;

import com.partshub.reporting.campaign.dto.CampaignFormDTO;
import com.partshub.reporting.campaign.entity.CampaignStatus;
import com.partshub.reporting.campaign.entity.PromotionalCampaign;
import com.partshub.reporting.campaign.repository.PromotionalCampaignRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link PromotionalCampaignService}.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Status is always auto-resolved based on dates (except CANCELLED).</li>
 *   <li>CANCELLED campaigns remain CANCELLED indefinitely.</li>
 *   <li>Business logic lives here – the controller stays thin.</li>
 * </ul>
 */
@Service
@Transactional
public class PromotionalCampaignServiceImpl implements PromotionalCampaignService {

    private final PromotionalCampaignRepository campaignRepository;

    // Constructor injection (no Lombok)
    public PromotionalCampaignServiceImpl(PromotionalCampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    // ── Read ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<PromotionalCampaign> getAllCampaigns() {
        List<PromotionalCampaign> campaigns = campaignRepository.findAll();
        // Refresh status for each campaign against today's date before displaying
        for (PromotionalCampaign campaign : campaigns) {
            if (campaign.getStatus() != CampaignStatus.CANCELLED) {
                CampaignStatus resolved = resolveStatus(campaign.getStartDate(), campaign.getEndDate());
                if (resolved != campaign.getStatus()) {
                    campaign.setStatus(resolved);
                    campaignRepository.save(campaign);
                }
            }
        }
        return campaigns;
    }

    @Override
    @Transactional
    public Optional<PromotionalCampaign> getCampaignById(Long id) {
        Optional<PromotionalCampaign> opt = campaignRepository.findById(id);
        opt.ifPresent(campaign -> {
            // Refresh status if not cancelled
            if (campaign.getStatus() != CampaignStatus.CANCELLED) {
                CampaignStatus resolved = resolveStatus(campaign.getStartDate(), campaign.getEndDate());
                if (resolved != campaign.getStatus()) {
                    campaign.setStatus(resolved);
                    campaignRepository.save(campaign);
                }
            }
        });
        return opt;
    }

    // ── Create ─────────────────────────────────────────────────────────────

    @Override
    public PromotionalCampaign createCampaign(CampaignFormDTO formDTO) {
        PromotionalCampaign campaign = new PromotionalCampaign();
        mapFormToEntity(formDTO, campaign);
        // System determines status automatically from the dates
        campaign.setStatus(resolveStatus(campaign.getStartDate(), campaign.getEndDate()));
        return campaignRepository.save(campaign);
    }

    // ── Update ─────────────────────────────────────────────────────────────

    @Override
    public PromotionalCampaign updateCampaign(Long id, CampaignFormDTO formDTO) {
        PromotionalCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + id));

        // Preserve CANCELLED status – do not overwrite with date-based resolution
        boolean wasCancelled = campaign.getStatus() == CampaignStatus.CANCELLED;

        mapFormToEntity(formDTO, campaign);

        if (wasCancelled) {
            // Keep the campaign cancelled even after editing its details
            campaign.setStatus(CampaignStatus.CANCELLED);
        } else {
            // Re-resolve status based on new dates
            campaign.setStatus(resolveStatus(campaign.getStartDate(), campaign.getEndDate()));
        }

        return campaignRepository.save(campaign);
    }

    // ── Cancel ─────────────────────────────────────────────────────────────

    @Override
    public void cancelCampaign(Long id) {
        PromotionalCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + id));
        campaign.setStatus(CampaignStatus.CANCELLED);
        campaignRepository.save(campaign);
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    @Override
    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new EntityNotFoundException("Campaign not found with id: " + id);
        }
        campaignRepository.deleteById(id);
    }

    // ── Dashboard count ────────────────────────────────────────────────────

    /**
     * Counts campaigns that are currently active based solely on today's date.
     *
     * <p>Uses a date-driven JPQL query instead of the stored {@code status} column
     * so the result is always up-to-date, even if individual campaigns have not
     * been loaded (and therefore not auto-refreshed) via the CRUD views.
     *
     * <p>Conditions: {@code startDate <= today AND endDate >= today AND status != CANCELLED}
     */
    @Override
    @Transactional(readOnly = true)
    public long countActiveCampaigns() {
        return campaignRepository.countCurrentlyActive(LocalDate.now(), CampaignStatus.CANCELLED);
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    /**
     * Resolves the campaign status based on the current date and the campaign's date range.
     *
     * <p>Logic:
     * <ul>
     *   <li>If today is before startDate   → UPCOMING</li>
     *   <li>If today is within the range   → ACTIVE</li>
     *   <li>If today is after endDate      → EXPIRED</li>
     * </ul>
     * CANCELLED is never returned here; it must be set explicitly via {@link #cancelCampaign}.
     */
    private CampaignStatus resolveStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return CampaignStatus.UPCOMING;
        } else if (!today.isAfter(endDate)) {
            return CampaignStatus.ACTIVE;
        } else {
            return CampaignStatus.EXPIRED;
        }
    }

    /**
     * Copies form DTO values into a PromotionalCampaign entity.
     * Used by both create and update operations to avoid duplication.
     */
    private void mapFormToEntity(CampaignFormDTO formDTO, PromotionalCampaign campaign) {
        campaign.setCampaignName(formDTO.getCampaignName().trim());
        campaign.setDescription(formDTO.getDescription() != null ? formDTO.getDescription().trim() : null);
        campaign.setSelectedProducts(formDTO.getSelectedProducts().trim());
        campaign.setDiscountPercentage(formDTO.getDiscountPercentage());
        campaign.setStartDate(formDTO.getStartDate());
        campaign.setEndDate(formDTO.getEndDate());
    }
}
