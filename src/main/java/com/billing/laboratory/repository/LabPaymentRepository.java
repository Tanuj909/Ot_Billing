package com.billing.laboratory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.laboratory.entity.LabPayment;

public interface LabPaymentRepository
        extends JpaRepository<LabPayment, Long> {

    List<LabPayment> findByLabOrderIdOrderByPaidAtDesc(Long labOrderId);
    
    List<LabPayment> findByPatientExternalIdOrderByPaidAtDesc(Long patientExternalId);
}
