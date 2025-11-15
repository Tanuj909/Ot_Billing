package com.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.OPDBillingDetails;

public interface OPDBillingRepository extends JpaRepository<OPDBillingDetails, Long>{
	
	Optional<OPDBillingDetails> findByAppointmentId(Long appountmentId);
}
