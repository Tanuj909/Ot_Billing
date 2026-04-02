package com.billing.model;

import java.time.LocalDateTime;

import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
	
//	@ManyToOne
//	@JoinColumn(name = "hospital_id")
//	private Hospital hospital;
	
	private Long hospitaExternallId;
	
	@Column(name = "lab_store_id")
	private Long labStoreId;
	
	private Long patientExternalId;
	
	private Long admissionId;
	
	private Long appointmentId;
	
	private Long emergencyId;
	
	private Long labOrderId;
	
    private Long otOperationId;         // 👈 NEW — OT system ka operationId
	
//	@ManyToOne
//	@JoinColumn(name = "patient_id")
//	private Patient patient;
	
	private String moduleType;
	private double totalAmount;
	
	//You need to tell JPA to store the enum as a string.
	//Otherwise it will store it as 0,1,2 in according the sequence you have mentioned enum in the class
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;
	
	@Enumerated(EnumType.STRING)
	private PaymentMode paymentMode;
	
	private String advancePaymentMode;
	
	private LocalDateTime billingDate = LocalDateTime.now();
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
    @PrePersist
    protected void onCreate() {
        billingDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
