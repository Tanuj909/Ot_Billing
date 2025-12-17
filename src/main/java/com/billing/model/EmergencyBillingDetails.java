package com.billing.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "emergency_billing_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmergencyBillingDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long emergencyId;
	
	@OneToOne
	@JoinColumn(name = "billing_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private BillingMaster billingMaster;
	
	private LocalDateTime createAt = LocalDateTime.now();
	private LocalDateTime updatedAt;
	
	//No.of days Patient Admitted
	private Long daysAdmitted;
	
	private Double advancePaid;

	private Double serviceCharges;
	
	private Double doctorFees;
	private Double monitoringCharges;
	private Double nursingCharges;
	private Double emergencyConsumable;
	private Double roomCharges;
	
	//Sum of all the charges
	private Double total;
	
	//Discount
	private Double discountPercentage;
	private Double discountAmount;
	
	//GST
	private Double itemGstAmount;
	
	//Total after discount
	private Double totalAfterDiscount;
	
	//Total After Discount & GST
	private Double totalAfterDiscountAndGst;
	
	//DUE(Due = total - advancePaid)
	private Double due;
	
	//(Payments Done by Patient/Family could be partial)
	private Double totalPayment;
	
	private String billingStatus = "ACTIVE";
	
	@OneToMany(mappedBy = "emergencyBillingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<EmergencyBillingItem> items = new ArrayList<>();
	
	
	private Double totalDoctorFees = 0.0;  // Add this field
	
	// ADD THIS:
	@OneToMany(mappedBy = "emergencyBillingDetails", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<EmergencyDoctorVisit> doctorVisits = new ArrayList<>();
	
	// ADD HELPER METHOD:
	public double calculateTotalDoctorFees() {
	    return getDoctorVisits().stream()
	            .mapToDouble(v -> v.getTotalFees() != null ? v.getTotalFees() : 0.0)
	            .sum();
	}
}
