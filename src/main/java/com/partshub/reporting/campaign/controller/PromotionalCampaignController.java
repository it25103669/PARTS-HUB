package com.partshub.reporting.campaign.controller;

import com.partshub.reporting.campaign.dto.CampaignFormDTO;
import com.partshub.reporting.campaign.entity.CampaignStatus;
import com.partshub.reporting.campaign.entity.PromotionalCampaign;
import com.partshub.reporting.campaign.service.PromotionalCampaignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Handles all HTTP requests for Promotional Campaign Management.
 *
 * <p>Routes:
 * <pre>
 *   GET  /campaigns              – list all campaigns
 *   GET  /campaigns/create       – show create form
 *   POST /campaigns/create       – submit create form
 *   GET  /campaigns/{id}         – view single campaign
 *   GET  /campaigns/{id}/edit    – show edit form
 *   POST /campaigns/{id}/edit    – submit edit form
 *   POST /campaigns/{id}/cancel  – cancel campaign
 *   POST /campaigns/{id}/delete  – delete campaign
 * </pre>
 *
 * <p>This controller is intentionally thin: all business logic
 * is delegated to {@link PromotionalCampaignService}.
 */
@Controller
@RequestMapping("/campaigns")
public class PromotionalCampaignController {

    private final PromotionalCampaignService campaignService;

    public PromotionalCampaignController(PromotionalCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // ── GET /campaigns ─────────────────────────────────────────────────────

    /**
     * Display the campaign list page with all campaigns.
     */
    @GetMapping
    public String listCampaigns(Model model) {
        model.addAttribute("campaigns", campaignService.getAllCampaigns());
        model.addAttribute("pageTitle", "Promotional Campaigns");
        model.addAttribute("activeMenu", "campaigns");
        return "campaigns/campaign-list";
    }

    // ── GET /campaigns/create ──────────────────────────────────────────────

    /**
     * Show the create campaign form.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("campaignForm", new CampaignFormDTO());
        model.addAttribute("pageTitle", "Create Promotional Campaign");
        model.addAttribute("activeMenu", "campaigns");
        model.addAttribute("formMode", "create");
        return "campaigns/campaign-form";
    }

    // ── POST /campaigns/create ─────────────────────────────────────────────

    /**
     * Process the create campaign form submission.
     * Validates dates (end >= start) in addition to Bean Validation constraints.
     */
    @PostMapping("/create")
    public String createCampaign(@Valid @ModelAttribute("campaignForm") CampaignFormDTO formDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        // Cross-field validation: end date must not be before start date
        if (formDTO.getStartDate() != null && formDTO.getEndDate() != null
                && formDTO.getEndDate().isBefore(formDTO.getStartDate())) {
            result.rejectValue("endDate", "endDate.beforeStart", "End date cannot be before start date.");
        }

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Create Promotional Campaign");
            model.addAttribute("activeMenu", "campaigns");
            model.addAttribute("formMode", "create");
            return "campaigns/campaign-form";
        }

        campaignService.createCampaign(formDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Campaign created successfully.");
        return "redirect:/campaigns";
    }

    // ── GET /campaigns/{id} ────────────────────────────────────────────────

    /**
     * Show the campaign detail/view page.
     */
    @GetMapping("/{id}")
    public String viewCampaign(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<PromotionalCampaign> optCampaign = campaignService.getCampaignById(id);
        if (optCampaign.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Campaign not found.");
            return "redirect:/campaigns";
        }
        PromotionalCampaign campaign = optCampaign.get();
        model.addAttribute("campaign", campaign);
        model.addAttribute("canCancel", campaign.getStatus() != CampaignStatus.CANCELLED
                && campaign.getStatus() != CampaignStatus.EXPIRED);
        model.addAttribute("canDelete", campaign.getStatus() == CampaignStatus.CANCELLED
                || campaign.getStatus() == CampaignStatus.EXPIRED);
        model.addAttribute("pageTitle", campaign.getCampaignName());
        model.addAttribute("activeMenu", "campaigns");
        return "campaigns/campaign-view";
    }

    // ── GET /campaigns/{id}/edit ───────────────────────────────────────────

    /**
     * Show the edit form pre-populated with existing campaign data.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<PromotionalCampaign> optCampaign = campaignService.getCampaignById(id);
        if (optCampaign.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Campaign not found.");
            return "redirect:/campaigns";
        }
        PromotionalCampaign campaign = optCampaign.get();

        // Pre-populate the form DTO from the existing entity
        CampaignFormDTO formDTO = new CampaignFormDTO();
        formDTO.setCampaignId(campaign.getCampaignId());
        formDTO.setCampaignName(campaign.getCampaignName());
        formDTO.setDescription(campaign.getDescription());
        formDTO.setSelectedProducts(campaign.getSelectedProducts());
        formDTO.setDiscountPercentage(campaign.getDiscountPercentage());
        formDTO.setStartDate(campaign.getStartDate());
        formDTO.setEndDate(campaign.getEndDate());

        model.addAttribute("campaignForm", formDTO);
        model.addAttribute("pageTitle", "Edit Promotional Campaign");
        model.addAttribute("activeMenu", "campaigns");
        model.addAttribute("formMode", "edit");
        return "campaigns/campaign-form";
    }

    // ── POST /campaigns/{id}/edit ──────────────────────────────────────────

    /**
     * Process the edit campaign form submission.
     */
    @PostMapping("/{id}/edit")
    public String updateCampaign(@PathVariable Long id,
                                 @Valid @ModelAttribute("campaignForm") CampaignFormDTO formDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        // Cross-field validation: end date must not be before start date
        if (formDTO.getStartDate() != null && formDTO.getEndDate() != null
                && formDTO.getEndDate().isBefore(formDTO.getStartDate())) {
            result.rejectValue("endDate", "endDate.beforeStart", "End date cannot be before start date.");
        }

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Promotional Campaign");
            model.addAttribute("activeMenu", "campaigns");
            model.addAttribute("formMode", "edit");
            return "campaigns/campaign-form";
        }

        try {
            campaignService.updateCampaign(id, formDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign updated successfully.");
            return "redirect:/campaigns/" + id;
        } catch (jakarta.persistence.EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Campaign not found.");
            return "redirect:/campaigns";
        }
    }

    // ── POST /campaigns/{id}/cancel ────────────────────────────────────────

    /**
     * Cancel a campaign. Sets status to CANCELLED permanently.
     */
    @PostMapping("/{id}/cancel")
    public String cancelCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            campaignService.cancelCampaign(id);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign cancelled successfully.");
        } catch (jakarta.persistence.EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Campaign not found.");
        }
        return "redirect:/campaigns";
    }

    // ── POST /campaigns/{id}/delete ────────────────────────────────────────

    /**
     * Delete a campaign.
     * Typically used for CANCELLED or EXPIRED campaigns during the viva demo.
     */
    @PostMapping("/{id}/delete")
    public String deleteCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            campaignService.deleteCampaign(id);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign deleted successfully.");
        } catch (jakarta.persistence.EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Campaign not found.");
        }
        return "redirect:/campaigns";
    }

}

