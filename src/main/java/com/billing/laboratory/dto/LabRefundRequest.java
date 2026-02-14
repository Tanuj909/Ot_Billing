package com.billing.laboratory.dto;

import lombok.Data;

@Data
public class LabRefundRequest {

    private Long labOrderId;
    private Double refundAmount;
    private String reason;
}
