package com.billing.emergency.dto;

import com.billing.enums.PaymentMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateEmergencyBillRequest {
    private Long emergencyId;                // Required
    private Long hospitaExternallId;         // From BillingMaster
    private Long patientExternalId;          // From BillingMaster
    private Double advancePaid;              // Optional initial advance
    private Long totalHoursAdmitted;
    private PaymentMode paymentMode;
	private Double doctorFees;
	private Double monitoringCharges;
	private Double nursingCharges;
	private Double emergencyConsumable;
	private Double roomChargesPerDay;
	// NEW: Discount percentage sent by Emergency microservice
    private Double discountPercentage;           // e.g., 10.0 for 10%
    // Add more if needed for initial setup (e.g., initial doctorFees, serviceCharges)
}