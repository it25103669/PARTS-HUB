package com.partshub.reporting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Legacy campaign placeholder controller.
 *
 * <p>The full Promotional Campaign CRUD is now handled by:
 * {@link com.partshub.reporting.campaign.controller.PromotionalCampaignController}
 * at {@code /campaigns}.
 *
 * <p>This class is intentionally kept minimal to avoid a duplicate mapping conflict.
 * It exists only because it was part of the original project scaffold.
 */
@Controller
public class CampaignController {

    /**
     * Redirect root /campaigns-old if anyone hits this legacy route directly.
     * The real mapping is in PromotionalCampaignController.
     */
    @GetMapping("/campaigns-legacy-unused")
    public String legacyRedirect() {
        return "redirect:/campaigns";
    }
}
