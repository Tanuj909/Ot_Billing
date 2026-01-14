package com.billing.laboratory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.laboratory.dto.LabTestBillItemDTO;
import com.billing.laboratory.entity.LabTestBilling;

public interface LabTestBillingRepository extends JpaRepository<LabTestBilling, Long>{

	List<LabTestBillItemDTO> findByLabBillingDetailsId(Long id);

}
