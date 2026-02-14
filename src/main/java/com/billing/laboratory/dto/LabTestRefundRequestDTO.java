package com.billing.laboratory.dto;

import lombok.Data;

@Data
public class LabTestRefundRequestDTO {
	
    private Long labOrderId;
    private Long orderItemId;
    private String reason;

}
