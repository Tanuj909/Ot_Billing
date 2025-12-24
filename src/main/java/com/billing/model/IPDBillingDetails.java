package com.billing.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.billing.service.IPDBillingService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ipd_billing_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@Builder
public class IPDBillingDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "billing_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private BillingMaster billingMaster;
	
	private Long admissionId;
	
	
//	IPD Room charges
    private Double roomCharges;
    private Double medicationCharges;
    private Double doctorFees;
    private Double nursingCharges;
    private Double diagnosticCharges;
    private Double procedureCharges;
    private Double foodCharges;
    private Double miscellaneousCharges;
    private Double otCharges = 0.0;
    private Long daysAdmitted;
    private Double total;
    
    @Column(nullable = true)
    private Double serviceCharges;
    
    
 // NEW REQUIRED FIELDS (as per your design)
    private Double advancePaid = 0.0;           // Collected at admission
    private Double totalPayments = 0.0;         // All payments during stay(Total payed amount)
    private Double totalCharges = 0.0;          // Sum of all charges
    private Double dueAmount = 0.0;             // totalCharges - (advance + payments)
    private Double dueTotalPayable= 0.0;
    private String billingStatus = "ACTIVE";    // ACTIVE / CLOSED
    
    
 // ADD THESE FIELDS
    private Double discountPercentage;
    private Double discountAmount;
    private Double gstPercentage;
    private Double gstAmount;
    private Double totalBeforeDiscount;
    private Double totalAfterDiscountAndGst ;  // final total
    private Double totalItemGstAmount = 0.0;
//    private Double payableAmount;
    
    private LocalDateTime updatedAt;
    
    private Double specialDiscountAmount = 0.0;     // New concession amount
    private Double specialDiscountPercentage= 0.0;
    private Double dueAfterSpecialDiscount= 0.0;
    
    @Column(nullable = true)
    private String specialDiscountReason;          // Optional: "Management Approval", "Charity", etc.

 // Relationships (optional for audit)
//    @OneToMany(mappedBy = "ipdBillingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private List<IPDPayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "ipdBillingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
    @JsonIgnore
    private List<IPDServiceUsage> services = new ArrayList<>();
    
    @OneToMany(mappedBy = "ipdBillingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<IPDDoctorVisit> doctorVisits = new ArrayList<>();

    @OneToMany(mappedBy = "ipdBillingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<IPDMedication> medications = new ArrayList<>();

    @OneToMany(mappedBy = "billingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<IpdPaymentHistory> paymentHistory = new ArrayList<>();
    
    
    public void recalculateDueAmount() {
        this.dueAmount = this.totalCharges - (this.advancePaid + this.totalPayments);
        if (this.dueAmount < 0) this.dueAmount = 0.0;
    }

    
}
