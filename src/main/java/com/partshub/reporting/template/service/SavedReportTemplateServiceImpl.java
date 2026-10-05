package com.partshub.reporting.template.service;

import com.partshub.reporting.template.dto.SavedReportTemplateDTO;
import com.partshub.reporting.template.entity.SavedReportTemplate;
import com.partshub.reporting.template.repository.SavedReportTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SavedReportTemplateServiceImpl implements SavedReportTemplateService {

    private final SavedReportTemplateRepository repository;

    public SavedReportTemplateServiceImpl(SavedReportTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedReportTemplate> getAllTemplates() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SavedReportTemplate> getTemplateById(Long id) {
        return repository.findById(id);
    }

    @Override
    public SavedReportTemplate createTemplate(SavedReportTemplateDTO dto) {
        SavedReportTemplate template = new SavedReportTemplate();
        mapDtoToEntity(dto, template);
        return repository.save(template);
    }

    @Override
    public SavedReportTemplate updateTemplate(Long id, SavedReportTemplateDTO dto) {
        SavedReportTemplate template = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));
        mapDtoToEntity(dto, template);
        return repository.save(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Template not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTemplateNameUnique(String templateName, Long excludeId) {
        if (excludeId == null) {
            return !repository.existsByTemplateNameIgnoreCase(templateName);
        } else {
            return !repository.existsByTemplateNameIgnoreCaseAndTemplateIdNot(templateName, excludeId);
        }
    }

    private void mapDtoToEntity(SavedReportTemplateDTO dto, SavedReportTemplate template) {
        template.setTemplateName(dto.getTemplateName().trim());
        template.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        template.setReportType(dto.getReportType());
        template.setDateRangeType(dto.getDateRangeType());
        template.setCustomStartDate(dto.getCustomStartDate());
        template.setCustomEndDate(dto.getCustomEndDate());
        template.setProductFilter(dto.getProductFilter() != null ? dto.getProductFilter().trim() : null);
        template.setCategoryFilter(dto.getCategoryFilter() != null ? dto.getCategoryFilter().trim() : null);
        template.setSupplierFilter(dto.getSupplierFilter() != null ? dto.getSupplierFilter().trim() : null);
        template.setChartType(dto.getChartType());
    }
}
