package com.billing.laboratory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.laboratory.entity.LabBillingDetails;
import com.billing.model.BillingMaster;

public interface LabBillingDetailsRepository extends JpaRepository<LabBillingDetails, Long>{

	Optional<LabBillingDetails> findByLabOrderId(Long labOrderId);
	
	Optional<LabBillingDetails> findByBillingMaster(BillingMaster billingMaster);

	Optional<LabBillingDetails> 
	findByLabOrderIdAndBillingMaster_Id(Long labOrderId, Long billingId);

}
