package com.billing.model;

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
    private Double roomCharges;
    private Double medicationCharges;
    private Double doctorFees;
    private Double nursingCharges;
    private Double diagnosticCharges;
//    private Double otCharges;
    private Double procedureCharges;
    private Double foodCharges;
    private Double miscellaneousCharges;
    private Long daysAdmitted;
    private Double total;
    
    @Column(nullable = true)
    private Double serviceCharges;
    
 // ADD THESE FIELDS
    private Double discountPercentage;
    private Double discountAmount;
    private Double gstPercentage;
    private Double gstAmount;
    private Double totalBeforeDiscount;
    private Double totalAfterDiscountAndGst ;  // final total
//    private Double payableAmount;

    
 // Add these to your existing IPDBillingDetails class

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


    // UPDATE total field comment: this is now final amount after GST
    
}
