package com.partshub.reporting.template.controller;

import com.partshub.reporting.dto.ReportFilterDTO;
import com.partshub.reporting.model.ChartType;
import com.partshub.reporting.model.DateRangeType;
import com.partshub.reporting.model.ReportType;
import com.partshub.reporting.template.dto.SavedReportTemplateDTO;
import com.partshub.reporting.template.entity.SavedReportTemplate;
import com.partshub.reporting.template.service.SavedReportTemplateService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports/templates")
public class SavedReportTemplateController {

    private final SavedReportTemplateService templateService;

    public SavedReportTemplateController(SavedReportTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public String listTemplates(Model model) {
        List<SavedReportTemplate> templates = templateService.getAllTemplates();
        model.addAttribute("templates", templates);
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Saved Report Templates");
        return "reports/templates/template-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        SavedReportTemplateDTO formDTO = new SavedReportTemplateDTO();
        // default chart type
        formDTO.setChartType(ChartType.NONE);

        model.addAttribute("templateForm", formDTO);
        model.addAttribute("formMode", "create");
        model.addAttribute("reportTypes", ReportType.values());
        model.addAttribute("dateRangeTypes", DateRangeType.values());
        model.addAttribute("chartTypes", ChartType.values());
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Create Template");
        return "reports/templates/template-form";
    }

    @PostMapping("/create")
    public String processCreateForm(@Valid @ModelAttribute("templateForm") SavedReportTemplateDTO formDTO,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        validateCustomDates(formDTO, bindingResult);
        
        if (!bindingResult.hasFieldErrors("templateName") && 
            !templateService.isTemplateNameUnique(formDTO.getTemplateName(), null)) {
            bindingResult.rejectValue("templateName", "error.templateName", "A report template with this name already exists.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            model.addAttribute("reportTypes", ReportType.values());
            model.addAttribute("dateRangeTypes", DateRangeType.values());
            model.addAttribute("chartTypes", ChartType.values());
            model.addAttribute("activeMenu", "reports");
            model.addAttribute("pageTitle", "Create Template");
            return "reports/templates/template-form";
        }

        templateService.createTemplate(formDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Template created successfully.");
        return "redirect:/reports/templates";
    }

    @GetMapping("/{id}")
    public String viewTemplate(@PathVariable Long id, Model model) {
        SavedReportTemplate template = templateService.getTemplateById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template Id:" + id));

        model.addAttribute("template", template);
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "View Template");
        return "reports/templates/template-view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        SavedReportTemplate template = templateService.getTemplateById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template Id:" + id));

        SavedReportTemplateDTO formDTO = new SavedReportTemplateDTO();
        formDTO.setTemplateId(template.getTemplateId());
        formDTO.setTemplateName(template.getTemplateName());
        formDTO.setDescription(template.getDescription());
        formDTO.setReportType(template.getReportType());
        formDTO.setDateRangeType(template.getDateRangeType());
        formDTO.setCustomStartDate(template.getCustomStartDate());
        formDTO.setCustomEndDate(template.getCustomEndDate());
        formDTO.setProductFilter(template.getProductFilter());
        formDTO.setCategoryFilter(template.getCategoryFilter());
        formDTO.setSupplierFilter(template.getSupplierFilter());
        formDTO.setChartType(template.getChartType());

        model.addAttribute("templateForm", formDTO);
        model.addAttribute("formMode", "edit");
        model.addAttribute("reportTypes", ReportType.values());
        model.addAttribute("dateRangeTypes", DateRangeType.values());
        model.addAttribute("chartTypes", ChartType.values());
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Edit Template");
        return "reports/templates/template-form";
    }

    @PostMapping("/{id}/edit")
    public String processEditForm(@PathVariable Long id,
                                  @Valid @ModelAttribute("templateForm") SavedReportTemplateDTO formDTO,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        validateCustomDates(formDTO, bindingResult);

        if (!bindingResult.hasFieldErrors("templateName") && 
            !templateService.isTemplateNameUnique(formDTO.getTemplateName(), id)) {
            bindingResult.rejectValue("templateName", "error.templateName", "A report template with this name already exists.");
        }

        if (bindingResult.hasErrors()) {
            formDTO.setTemplateId(id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("reportTypes", ReportType.values());
            model.addAttribute("dateRangeTypes", DateRangeType.values());
            model.addAttribute("chartTypes", ChartType.values());
            model.addAttribute("activeMenu", "reports");
            model.addAttribute("pageTitle", "Edit Template");
            return "reports/templates/template-form";
        }

        templateService.updateTemplate(id, formDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Template updated successfully.");
        return "redirect:/reports/templates/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        templateService.deleteTemplate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Template deleted successfully.");
        return "redirect:/reports/templates";
    }

    @GetMapping("/{id}/use")
    public String useTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        SavedReportTemplate template = templateService.getTemplateById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template Id:" + id));

        ReportFilterDTO filter = new ReportFilterDTO();
        filter.setReportType(template.getReportType());

        if (template.getDateRangeType() == DateRangeType.CUSTOM) {
            filter.setDateFrom(template.getCustomStartDate());
            filter.setDateTo(template.getCustomEndDate());
        } else if (template.getDateRangeType() == DateRangeType.TODAY) {
            filter.setDateFrom(LocalDate.now());
            filter.setDateTo(LocalDate.now());
        } else if (template.getDateRangeType() == DateRangeType.THIS_WEEK) {
            filter.setDateFrom(LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
            filter.setDateTo(LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)));
        } else if (template.getDateRangeType() == DateRangeType.THIS_MONTH) {
            filter.setDateFrom(LocalDate.now().withDayOfMonth(1));
            filter.setDateTo(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        }

        filter.setProductName(template.getProductFilter());
        filter.setCategory(template.getCategoryFilter());
        filter.setSupplierName(template.getSupplierFilter());

        redirectAttributes.addFlashAttribute("filter", filter);
        redirectAttributes.addFlashAttribute("successMessage", "Template '" + template.getTemplateName() + "' applied.");
        return "redirect:/reports";
    }

    private void validateCustomDates(SavedReportTemplateDTO formDTO, BindingResult bindingResult) {
        if (formDTO.getDateRangeType() == DateRangeType.CUSTOM) {
            if (formDTO.getCustomStartDate() == null) {
                bindingResult.rejectValue("customStartDate", "error.customStartDate", "Start date is required for Custom Date Range.");
            }
            if (formDTO.getCustomEndDate() == null) {
                bindingResult.rejectValue("customEndDate", "error.customEndDate", "End date is required for Custom Date Range.");
            }
            if (formDTO.getCustomStartDate() != null && formDTO.getCustomEndDate() != null) {
                if (formDTO.getCustomEndDate().isBefore(formDTO.getCustomStartDate())) {
                    bindingResult.rejectValue("customEndDate", "error.customEndDate", "End date cannot be before start date.");
                }
            }
        }
    }
}
