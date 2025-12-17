package com.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.billing.model.OPDBillingDetails;
import jakarta.persistence.LockModeType;

public interface OPDBillingRepository extends JpaRepository<OPDBillingDetails, Long>{
	
//	@Lock(LockModeType.PESSIMISTIC_WRITE)
//	@Query("SELECT o FROM OPDBillingDetails o WHERE o.appointmentId = :appointmentId")
	Optional<OPDBillingDetails> findByAppointmentId(@Param("appointmentId") Long appountmentId);
}
