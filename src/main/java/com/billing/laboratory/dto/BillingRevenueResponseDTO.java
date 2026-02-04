package com.billing.laboratory.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillingRevenueResponseDTO {

    private String fromDate;
    private String toDate;

    private RevenueSummaryDTO summary;
    private List<StoreRevenueDTO> storeWise;
}
