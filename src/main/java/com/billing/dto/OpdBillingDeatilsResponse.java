package com.billing.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OpdBillingDeatilsResponse {
	
	private Long id;
	private BillingResponseDTO billingResponseDTO;
    private Double doctorFee;
	private Double emergencyFee;
	private Double dressing;
	private Double injection;
	private Double minorProcedure;
	private Double totalFees;
	private LocalDateTime visitDate;
	private Double serviceCharges;
	private Double amountToPay;
	private Double payableAmount;
}
