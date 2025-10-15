package com.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.IPDBillingDetails;

public interface IPDBillingRepository extends JpaRepository<IPDBillingDetails, Long>{

}
