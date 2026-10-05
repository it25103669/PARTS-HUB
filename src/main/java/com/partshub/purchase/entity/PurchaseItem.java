package com.partshub.purchase.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonBackReference
    private PurchaseOrder purchaseOrder;

    @Column(name = "part_name", nullable = false, length = 200)
    private String partName;

    @Column(name = "qty_ordered", nullable = false)
    private Integer qtyOrdered = 1;

    @Column(name = "qty_received")
    private Integer qtyReceived = 0;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(name = "condition_status", length = 50)
    private String conditionStatus = "GOOD"; // GOOD or DAMAGED

    public PurchaseItem() {}

    public void calculateLineTotal() {
        if (qtyOrdered != null && unitCost != null) {
            this.lineTotal = unitCost.multiply(BigDecimal.valueOf(qtyOrdered));
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public Integer getQtyOrdered() { return qtyOrdered; }
    public void setQtyOrdered(Integer qtyOrdered) { this.qtyOrdered = qtyOrdered; }

    public Integer getQtyReceived() { return qtyReceived; }
    public void setQtyReceived(Integer qtyReceived) { this.qtyReceived = qtyReceived; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public String getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }
}
