package com.billing.ipd.repository;
//import java.util.*;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.ipd.entity.IPDRoomAllocation;
import com.billing.model.IPDBillingDetails;

public interface IPDRoomAllocationRepository extends JpaRepository<IPDRoomAllocation, Long> {
    List<IPDRoomAllocation> findByIpdBillingDetailsId(Long billingDetailsId);
//    Optional<IPDRoomAllocation> findByIpdBillingDetailsIdAndReleaseDateIsNull(Long billingDetailsId);
	Optional<IPDRoomAllocation> findByIpdBillingDetailsAndReleaseDateIsNull(IPDBillingDetails billing);
	List<IPDRoomAllocation> findByIpdBillingDetails(IPDBillingDetails billing);
}