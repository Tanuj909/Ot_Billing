package com.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.OPDBillingDetails;

public interface OPDBillingRepository extends JpaRepository<OPDBillingDetails, Long>{

}
