package com.billing.ot.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.billing.model.BillingMaster;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//1. OTBillingDetails
@Entity
@Table(name = "ot_billing_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTBillingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_id", nullable = false)
    private BillingMaster billingMaster;

    private Long operationExternalId;       // OT system ka operationId
    private String operationReference;      // OT-2024-001
    private Long hospitalExternalId;
    private Long patientExternalId;
    private Long admissionId;

    // Totals — auto calculated
    private Double totalStaffCharges;
    private Double totalRoomCharges;
    private Double totalItemCharges;
    private Double totalDiscountAmount;
    private Double totalGstAmount;
    private Double grossAmount;             // before discount
    private Double totalAmount;             // final amount
    private Double advancePaid;
    private Double due;                     // totalAmount - advancePaid

    private String billingStatus;           // ACTIVE, CLOSED, CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "otBillingDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OTStaffBilling> staffCharges = new ArrayList<>();

    @OneToOne(mappedBy = "otBillingDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OTRoomBilling roomCharges;
    
    // Recovery Room (Post-OT)
    @OneToOne(mappedBy = "otBillingDetails", 
              cascade = CascadeType.ALL, 
              fetch = FetchType.LAZY,
              orphanRemoval = true)
    private OTRecoveryRoomBilling recoveryRoomCharges;
    
 // Total field (already aapne mention kiya tha)
    private Double totalRecoveryCharges = 0.0;
    
//    Total Doctor Visit Fees
    private Double totalDoctorVisitCharges = 0.0;
    

    @OneToMany(
    	    mappedBy = "otBillingDetails",
    	    cascade = CascadeType.ALL,
    	    fetch = FetchType.LAZY,
    	    orphanRemoval = true // 🔥 ADD THIS
    	)
    	private List<OTItemBilling> itemCharges = new ArrayList<>();
    
    @OneToMany(
    	    mappedBy = "otBillingDetails",
    	    cascade = CascadeType.ALL,
    	    fetch = FetchType.LAZY,
    	    orphanRemoval = true
    	)
    	private List<OTDoctorVisitBilling> doctorVisits = new ArrayList<>();
    
    @OneToMany(mappedBy = "otBillingDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OTPayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "otBillingDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OTRefund> refunds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        billingStatus = "ACTIVE";
        advancePaid = 0.0;
        totalStaffCharges = 0.0;
        totalRoomCharges = 0.0;
        totalRecoveryCharges = 0.0;        // ← ADD THIS
        totalDoctorVisitCharges = 0.0;
        totalItemCharges = 0.0;
        totalDiscountAmount = 0.0;
        totalGstAmount = 0.0;
        grossAmount = 0.0;
        totalAmount = 0.0;
        due = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}