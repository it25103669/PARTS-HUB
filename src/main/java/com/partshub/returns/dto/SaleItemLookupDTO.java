package com.partshub.returns.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SaleItemLookupDTO {
    private String partName;
    private String skuCode;
    private Integer originalQuantity;
    private Integer returnedQuantity;
    private Integer returnableQuantity;
    private BigDecimal unitPrice;
    private Integer warrantyPeriodMonths;
    private LocalDate warrantyExpiryDate;
    private Boolean inWarranty;

    public SaleItemLookupDTO() {}

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

    public Integer getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(Integer originalQuantity) { this.originalQuantity = originalQuantity; }

    public Integer getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(Integer returnedQuantity) { this.returnedQuantity = returnedQuantity; }

    public Integer getReturnableQuantity() { return returnableQuantity; }
    public void setReturnableQuantity(Integer returnableQuantity) { this.returnableQuantity = returnableQuantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getWarrantyPeriodMonths() { return warrantyPeriodMonths; }
    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) { this.warrantyPeriodMonths = warrantyPeriodMonths; }

    public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }

    public Boolean getInWarranty() { return inWarranty; }
    public void setInWarranty(Boolean inWarranty) { this.inWarranty = inWarranty; }
}
