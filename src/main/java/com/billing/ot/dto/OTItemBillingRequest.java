package com.billing.ot.dto;

import com.billing.ot.enums.OTItemType;

import lombok.Data;

@Data
public class OTItemBillingRequest {
    private Long operationExternalId;
    private Long itemExternalId;
    private OTItemType itemType;
    private String itemName;
    private String itemCode;
    private String hsnCode;
    private Integer quantity;
    private Double unitPrice;
    private Double discountPercent;
    private Double gstPercent;
}
