package com.billing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IPDBillingDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "billing_id")
	private BillingMaster billingMaster;
	
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
    
}
