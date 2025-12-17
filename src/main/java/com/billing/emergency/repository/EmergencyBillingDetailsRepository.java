package com.billing.emergency.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.billing.model.EmergencyBillingDetails;

public interface EmergencyBillingDetailsRepository extends JpaRepository<EmergencyBillingDetails, Long> {
	
	Optional<EmergencyBillingDetails> findByEmergencyId(Long emergencyId);

}
