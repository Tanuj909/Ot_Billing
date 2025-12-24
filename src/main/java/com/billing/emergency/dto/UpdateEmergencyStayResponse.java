// com.billing.emergency.dto.UpdateEmergencyStayResponse.java
package com.billing.emergency.dto;

import lombok.Data;

@Data
public class UpdateEmergencyStayResponse {
    private Long emergencyId;
    private Long totalHoursAdmitted;
    private Double doctorFees;              // unchanged, shown for reference
    private Double monitoringCharges;       // recalculated
    private Double nursingCharges;          // recalculated
    private Double roomCharges;             // recalculated from existing per-day
    private Double emergencyConsumable;     // unchanged (one-time)
    private Double itemsTotal;
    private Double itemsGst;
    private Double discountApplied;
    private Double finalBillAmount;
    private Double totalPaid;
    private Double dueAmount;
    private String message = "Emergency stay updated and bill recalculated successfully";
}