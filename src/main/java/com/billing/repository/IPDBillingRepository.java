package com.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.dto.IpdBillRequestDTO;
import com.billing.model.IPDBillingDetails;

public interface IPDBillingRepository extends JpaRepository<IPDBillingDetails, Long>{
//	IPDBillingDetails findByAdmissionId(Long admissionId);
	
//	IpdBillRequestDTO findBillingByAdmissionId(Long admissionId);
	
	 Optional<IPDBillingDetails> findByAdmissionId(Long admissionId);
}
