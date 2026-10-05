package com.partshub.reporting.campaign.repository;

import com.partshub.reporting.campaign.entity.CampaignStatus;
import com.partshub.reporting.campaign.entity.PromotionalCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link PromotionalCampaign}.
 *
 * <p>Hibernate automatically maps this to the {@code promotional_campaign} table.
 * All basic CRUD operations (save, findById, findAll, deleteById) are
 * inherited from {@link JpaRepository}.
 */
@Repository
public interface PromotionalCampaignRepository extends JpaRepository<PromotionalCampaign, Long> {

    /**
     * Find all campaigns with a specific status.
     * Used by the dashboard to count ACTIVE campaigns and to filter campaign lists.
     *
     * @param status the CampaignStatus to filter by
     * @return list of campaigns matching the given status
     */
    List<PromotionalCampaign> findByStatus(CampaignStatus status);

    /**
     * Count campaigns that are currently active according to the current date.
     *
     * <p>A campaign is considered currently active when:
     * <ul>
     *   <li>{@code startDate <= today} – the campaign has already started</li>
     *   <li>{@code endDate   >= today} – the campaign has not yet ended</li>
     *   <li>{@code status != CANCELLED} – the campaign was not manually cancelled</li>
     * </ul>
     *
     * <p>This query is date-driven and does NOT rely on the stored {@code status}
     * column, so the dashboard count stays accurate even when campaigns have not
     * been individually loaded (and therefore not auto-refreshed) via the CRUD views.
     *
     * @param today  the reference date (pass {@code LocalDate.now()} from the service)
     * @return number of currently active campaigns
     */
    @Query("SELECT COUNT(c) FROM PromotionalCampaign c "
         + "WHERE c.startDate <= :today "
         + "AND   c.endDate   >= :today "
         + "AND   c.status    <> :cancelled")
    long countCurrentlyActive(@Param("today")     LocalDate today,
                              @Param("cancelled") CampaignStatus cancelled);
}
