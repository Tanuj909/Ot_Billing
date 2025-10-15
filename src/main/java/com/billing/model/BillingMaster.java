package com.billing.model;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "billing_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "hospital_id")
	private Hospital hospital;
	
	@ManyToOne
	@JoinColumn(name = "patient_id")
	private Patient patient;
	
	private String moduleType;
	private double totalAmount;
	private String status;
	
	private LocalDateTime billingDate = LocalDateTime.now();
}
