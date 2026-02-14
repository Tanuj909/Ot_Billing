package com.billing.laboratory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabRefundResponse {

    private Long refundId;
    private Long labOrderId;
    private Double refundAmount;
    private String refundStatus;
}
