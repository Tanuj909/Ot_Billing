package com.billing.laboratory.entity;

import java.time.LocalDateTime;
import com.billing.model.BillingMaster;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "lab_billing_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LabBillingDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long labOrderId;
	
	@OneToOne
	@JoinColumn(name = "billing_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private BillingMaster billingMaster;
	
	private LocalDateTime createAt = LocalDateTime.now();
	private LocalDateTime updatedAt;
	
	private Double testCharges;
	
	//DUE(Due = total - advancePaid)
	private Double due;
	
	//(Payments Done by Patient/Family could be partial)
	private Double totalPayment;
	
	private String billingStatus = "ACTIVE";
	
	private Double testGstAmount;
	
	private Double discountPercentage;
	
	private Double discountAmount;
	
}
