package com.billing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.model.IpdPaymentHistory;

//IpdPaymentHistoryRepository.java
public interface IpdPaymentHistoryRepository extends JpaRepository<IpdPaymentHistory, Long> {
 List<IpdPaymentHistory> findByAdmissionIdOrderByPaymentDateDesc(Long admissionId);
}
