package com.billing.laboratory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueSummaryDTO {

    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
}
