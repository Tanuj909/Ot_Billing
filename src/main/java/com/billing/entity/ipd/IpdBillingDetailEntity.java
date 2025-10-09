package com.billing.entity.ipd;

import com.billing.entity.Billing;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "BillingRepository")
public class IpdBillingDetailEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idLong;
	
	@ManyToOne
	@JoinColumn(name = "billing_id")
	private Billing billing;
	
    private double roomCharges;
    private double doctorFees;
    private double medicationCharges;
    private double testCharges;
	public Long getIdLong() {
		return idLong;
	}
	public void setIdLong(Long idLong) {
		this.idLong = idLong;
	}
	public Billing getBilling() {
		return billing;
	}
	public void setBilling(Billing billing) {
		this.billing = billing;
	}
	public double getRoomCharges() {
		return roomCharges;
	}
	public void setRoomCharges(double roomCharges) {
		this.roomCharges = roomCharges;
	}
	public double getDoctorFees() {
		return doctorFees;
	}
	public void setDoctorFees(double doctorFees) {
		this.doctorFees = doctorFees;
	}
	public double getMedicationCharges() {
		return medicationCharges;
	}
	public void setMedicationCharges(double medicationCharges) {
		this.medicationCharges = medicationCharges;
	}
	public double getTestCharges() {
		return testCharges;
	}
	public void setTestCharges(double testCharges) {
		this.testCharges = testCharges;
	}
	
    

}
