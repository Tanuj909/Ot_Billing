package com.billing.ot.dto;

import java.util.List;
import java.util.Map;

import com.billing.ot.enums.OTItemType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTItemBillingSummaryResponse {
    private Long operationExternalId;
    private List<OTItemBillingResponse> allItems;
    private Map<OTItemType, List<OTItemBillingResponse>> byItemType;
    private Map<OTItemType, Double> totalByItemType;
    private Double grandTotal;
}