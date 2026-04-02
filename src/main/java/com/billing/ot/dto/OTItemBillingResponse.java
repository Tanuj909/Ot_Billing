package com.billing.ot.dto;

import java.time.LocalDateTime;

import com.billing.ot.enums.OTItemType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTItemBillingResponse {
    private Long id;
    private Long otBillingDetailsId;
    private Long itemExternalId;
    private OTItemType itemType;
    private String itemName;
    private String itemCode;
    private String hsnCode;
    private Integer quantity;
    private Double unitPrice;
    private Double discountPercent;
    private Double discountAmount;
    private Double priceAfterDiscount;
    private Double gstPercent;
    private Double gstAmount;
    private Double totalAmount;
    private LocalDateTime serviceAddedAt;
    private LocalDateTime createdAt;
}