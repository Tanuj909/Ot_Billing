package com.billing.emergency.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillInActiveRequest {
	
	@NotNull(message = "Emergency Id is Required!")
	private Long emergencyId;
	
	// Optional note for overpayment or special case
	private String closureRemarks;
	
}
