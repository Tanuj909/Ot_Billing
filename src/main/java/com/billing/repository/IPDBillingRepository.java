package com.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.billing.dto.IpdBillRequestDTO;
import com.billing.model.IPDBillingDetails;

import jakarta.persistence.LockModeType;

public interface IPDBillingRepository extends JpaRepository<IPDBillingDetails, Long>{
//	IPDBillingDetails findByAdmissionId(Long admissionId);
	
//	IpdBillRequestDTO findBillingByAdmissionId(Long admissionId);
	
//	@Lock(LockModeType.PESSIMISTIC_WRITE)
//	@Query("SELECT o FROM IPDBillingDetails o WHERE o.admissionId = :admissionId")
	 Optional<IPDBillingDetails> findByAdmissionId(@Param("admissionId") Long admissionId);
}
