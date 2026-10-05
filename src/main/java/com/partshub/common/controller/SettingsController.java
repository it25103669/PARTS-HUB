package com.partshub.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Application Settings – Placeholder Controller
 * Shared controller under the common package.
 */
@Controller
@RequestMapping("/settings")
public class SettingsController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle",  "Settings");
        model.addAttribute("activeMenu", "settings");
        return "settings/index";
    }
}
