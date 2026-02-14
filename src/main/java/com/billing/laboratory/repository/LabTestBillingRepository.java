package com.billing.laboratory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.billing.laboratory.entity.LabBillingDetails;
import com.billing.laboratory.entity.LabTestBilling;

public interface LabTestBillingRepository extends JpaRepository<LabTestBilling, Long>{

//	List<LabTestBillItemDTO> findByLabBillingDetailsId(Long id);
	List<LabTestBilling> findByLabBillingDetailsId(Long billingDetailsId);
	
	void deleteByLabBillingDetails(LabBillingDetails labBillingDetails);

	Optional<LabTestBilling> 
	findByLabBillingDetails_IdAndOrderItemId(Long labBillingId, Long orderItemId);

}
