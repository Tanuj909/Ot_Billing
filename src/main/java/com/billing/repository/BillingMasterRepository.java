package com.billing.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.billing.model.BillingMaster;

public interface BillingMasterRepository extends JpaRepository<BillingMaster, Long> {
	
	//For IPD
	Optional<BillingMaster> findByAdmissionId(Long admissionId);
	
	//For OPD
	Optional<BillingMaster> findByAppointmentId(Long appointmentId);

	boolean existsByAppointmentId(Long appointmentId);
	
}
