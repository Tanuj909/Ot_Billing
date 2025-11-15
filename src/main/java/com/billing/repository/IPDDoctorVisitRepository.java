package com.billing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.IPDDoctorVisit;

//IPDDoctorVisitRepository.java
public interface IPDDoctorVisitRepository extends JpaRepository<IPDDoctorVisit, Long> {
 List<IPDDoctorVisit> findByIpdBillingDetailsId(Long ipdBillingId);
}