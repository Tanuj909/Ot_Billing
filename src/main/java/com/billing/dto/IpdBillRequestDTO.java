package com.billing.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.billing.enums.PaymentStatus;
import com.billing.model.IPDDoctorVisit;
import com.billing.model.IPDMedication;
import com.billing.model.IPDServiceUsage;


@Data
@Getter
@Setter
public class IpdBillRequestDTO {
    
	private Long patientExternalId;
    private Long hospitalExternalId;
    private Long admissionId;
    
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private Long daysAdmitted;
    
    private Integer bedNumber;
    private String roomNumber;
    private Double roomRatePerDay;
    private Double doctorFee;
    private Double medicationCharges;
    private double ProcedureCharges;
    private Double nursingCharges;
    private Double diagnosticCharges;

    // =========================
    // FOOD (SPLIT)
    // =========================
    private Double foodCharges;
    private Double patientFoodCharges;     // GST EXEMPT
    private Double attendantFoodCharges;   // GST already calculated in gstAmount
    
    
    private Double miscellaneousCharges;
    
    private PaymentStatus paymentStatus;
    private Double advanceAmount;      // ← NEW, optional
    private String advancePaymentMode; // ← NEW, optional
    
 // ADD THESE FIELDS
    private Double discountPercentage;
    
    // =========================
    // TAX
    // =========================
    private Double gstPercentage;   // ✅ FINAL GST ONLY
    
    private List<IPDServiceUsage> ipdService;
//    private List<IPDDoctorVisit> doctorVisits;
//    private List<IPDMedication> medications;


}
