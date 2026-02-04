package com.billing.laboratory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoreRevenueDTO {

    private Long storeId;
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
}
