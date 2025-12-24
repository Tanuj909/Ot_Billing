package com.billing.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.billing.model.IPDServiceUsage;

import lombok.Data;

@Data
public class IpdBillingDetailsResponse {
    private Long id;
    private BillingMasterDTO billingMaster;
    private Long admissionId;
    private Double roomCharges;
    private Double medicationCharges;
    private Double doctorFees;
    private Double nursingCharges;
    private Double diagnosticCharges;
    private Double procedureCharges;
    private Double foodCharges;
    private Double miscellaneousCharges;
    private Long daysAdmitted;
    private Double total;
 // ADD NESTED DTO FIELDS
    private Double discountPercentage;
    private Double discountAmount;
    private Double gstPercentage;
    private Double gstAmount;
    private Double totalBeforeDiscount;
    private Double totalAfterDiscountAndGst;
    private List<IPDServiceUsage> ipdServices;
    private Double advanceAmount;      // ← NEW, optional
    private Double dueAmmount;
    private Double dueTotalPayable;
    private Double totalPayedAmmount;
    private String billingStatus;

    
    // ADD GETTERS/SETTERS

    @Data
    public static class BillingMasterDTO {
        private Long id;
        private Long hospitaExternallId;
        private Long patientExternalId;
        private Long admissionId;
        private String moduleType;
        private Double totalAmount;
        private String paymentStatus;
        private String paymentMode;
        private String advancePaymentMode; // ← NEW, optional
        private LocalDateTime billingDate;
    }
}