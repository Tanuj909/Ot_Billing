package com.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.billing.model.IPDDoctorVisit;
import com.billing.model.IPDMedication;

public interface IPDMedicationRepository extends JpaRepository<IPDMedication, Long> {
    List<IPDMedication> findByIpdBillingDetailsId(Long ipdBillingId);
}