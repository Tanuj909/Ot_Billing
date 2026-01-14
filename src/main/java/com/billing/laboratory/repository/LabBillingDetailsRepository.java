package com.billing.laboratory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.laboratory.entity.LabBillingDetails;

public interface LabBillingDetailsRepository extends JpaRepository<LabBillingDetails, Long>{

	Optional<LabBillingDetails> findByLabOrderId(Long labOrderId);

}
