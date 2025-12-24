// com.billing.emergency.dto.GetEmergencyBillResponse.java
package com.billing.emergency.dto;

import com.billing.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GetEmergencyBillResponse {

    private Long emergencyId;
    private Long billingId;                    // BillingMaster ID
    private Long billingDetailsId;             // EmergencyBillingDetails ID

    private LocalDateTime billingDate;
    private String billingStatus;              // ACTIVE / CLOSED
    private PaymentStatus paymentStatus;

    // Patient & Hospital (external IDs)
    private Long patientExternalId;
    private Long hospitaExternallId;

    // Stay details
	private Long totalHoursAdmitted; 

    // Fixed Charges
    private Double totalDoctorFees;  // Renamed from doctorFees for clarity

    private List<EmergencyDoctorVisitDto> doctorVisits;  // New breakdown

    // Items (services added)
    private List<EmergencyBillingItemDto> items;

    // Totals
    private Double baseTotal;                   // Fixed charges only
    private Double itemsTotal;                  // Sum of item amounts (excl. GST)
    private Double totalBeforeDiscount;

    // Discount
    private Double discountPercentage;
    private Double discountAmount;

    // GST
    private Double itemsGstAmount;

    // Final Amounts
    private Double totalAfterDiscount;
    private Double finalBillAmount;             // totalAfterDiscount + GST
    private Double totalPaid;                   // advance + partial payments
    private Double dueAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String message = "Emergency bill retrieved successfully";
}