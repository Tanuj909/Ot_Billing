package com.billing.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class AddServicesRequest {
    private Long ipdBillingId;
    private List<ServiceItem> services;
    
    @Data
    public static class ServiceItem {
        private String serviceName;
        private Double price;
        private Integer quantity;
        private LocalDateTime serviceAddDate;
        private Double gstPercentage;  // Must be sent from frontend/service master
    }
}