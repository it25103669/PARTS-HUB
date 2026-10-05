package com.partshub.reporting.template.repository;

import com.partshub.reporting.template.entity.SavedReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedReportTemplateRepository extends JpaRepository<SavedReportTemplate, Long> {

    boolean existsByTemplateNameIgnoreCase(String templateName);

    boolean existsByTemplateNameIgnoreCaseAndTemplateIdNot(String templateName, Long templateId);
}
