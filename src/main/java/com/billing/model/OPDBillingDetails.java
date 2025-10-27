package com.billing.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opd_billing_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OPDBillingDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Double doctorFee;
	
	private Double emergencyFee;
	
	private Double dressing;
	
	private Double injection;
	
	private Double minorProcedure;
	
	private Double total;
	
	private LocalDate visitDate;
	
	@OneToOne
	@JoinColumn(name = "billing_id")
	private BillingMaster billingMaster;
	
}
