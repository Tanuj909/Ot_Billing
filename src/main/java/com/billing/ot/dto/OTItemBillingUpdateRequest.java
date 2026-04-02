package com.billing.ot.dto;

import lombok.Data;

@Data
public class OTItemBillingUpdateRequest {
    private Integer quantity;
    private Double unitPrice;
    private Double discountPercent;
    private Double gstPercent;
}
