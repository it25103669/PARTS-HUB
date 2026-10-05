package com.partshub.reporting.template.service;

import com.partshub.reporting.template.dto.SavedReportTemplateDTO;
import com.partshub.reporting.template.entity.SavedReportTemplate;

import java.util.List;
import java.util.Optional;

public interface SavedReportTemplateService {

    List<SavedReportTemplate> getAllTemplates();

    Optional<SavedReportTemplate> getTemplateById(Long id);

    SavedReportTemplate createTemplate(SavedReportTemplateDTO dto);

    SavedReportTemplate updateTemplate(Long id, SavedReportTemplateDTO dto);

    void deleteTemplate(Long id);

    boolean isTemplateNameUnique(String templateName, Long excludeId);
}
