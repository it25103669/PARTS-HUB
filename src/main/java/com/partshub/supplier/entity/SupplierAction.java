package com.partshub.supplier.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_actions")
public class SupplierAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "action_category", nullable = false, length = 20)
    private String actionCategory; // POSITIVE or NEGATIVE

    @Column(name = "action_name", nullable = false, length = 150)
    private String actionName;

    @Column(name = "score_impact", nullable = false)
    private Integer scoreImpact;

    @Column(name = "po_reference", length = 100)
    private String poReference;

    @Column(name = "action_date", nullable = false)
    private LocalDate actionDate = LocalDate.now();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupplierAction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getActionCategory() { return actionCategory; }
    public void setActionCategory(String actionCategory) { this.actionCategory = actionCategory; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public Integer getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(Integer scoreImpact) { this.scoreImpact = scoreImpact; }

    public String getPoReference() { return poReference; }
    public void setPoReference(String poReference) { this.poReference = poReference; }

    public LocalDate getActionDate() { return actionDate; }
    public void setActionDate(LocalDate actionDate) { this.actionDate = actionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
