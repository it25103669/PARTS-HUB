package com.partshub.reporting.campaign.controller;

import com.partshub.reporting.campaign.dto.CampaignFormDTO;
import com.partshub.reporting.campaign.entity.PromotionalCampaign;
import com.partshub.reporting.campaign.service.PromotionalCampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignApiController {

    private final PromotionalCampaignService campaignService;

    public CampaignApiController(PromotionalCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public List<PromotionalCampaign> getCampaigns() {
        return campaignService.getAllCampaigns();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCampaign(@PathVariable Long id) {
        return campaignService.getCampaignById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCampaign(@RequestBody CampaignFormDTO formDTO) {
        try {
            PromotionalCampaign created = campaignService.createCampaign(formDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to create campaign"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampaign(@PathVariable Long id, @RequestBody CampaignFormDTO formDTO) {
        try {
            PromotionalCampaign updated = campaignService.updateCampaign(id, formDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to update campaign"));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelCampaign(@PathVariable Long id) {
        try {
            campaignService.cancelCampaign(id);
            return ResponseEntity.ok(Map.of("message", "Campaign cancelled successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to cancel campaign"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCampaign(@PathVariable Long id) {
        try {
            campaignService.deleteCampaign(id);
            return ResponseEntity.ok(Map.of("message", "Campaign deleted successfully", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to delete campaign"));
        }
    }
}
