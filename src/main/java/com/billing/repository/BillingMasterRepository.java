package com.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;

public interface BillingMasterRepository extends JpaRepository<BillingMaster, Long> {
	
	Optional<BillingMaster> findByAdmissionId(Long admissionId);
	
//	List<BillingMaster> findByPatient_Id(Long patientId);
//	
//	List<BillingMaster> findByHospital_Id(Long hospitalId);
	
//	@Query("SELECT SUM(b.totalAmount) FROM BillingMaster b WHERE b.hospital.id = :hospitalId")
//	Double getTotalBillingByHospitalId(@Param("hospitalId") Long hospitalId);
//	
//	List<BillingMaster> findBillingByHospital_IdAndModuleType(Long hospitalId, String moduleType);
//	
//	@Query("SELECT b FROM BillingMaster b WHERE b.hospital.id = :hospitalId AND b.paymentStatus = :status")
//	List<BillingMaster> findBillingByHospitalIdAndPaymentStatus(
//	        @Param("hospitalId") Long hospitalId,
//	        @Param("status") PaymentStatus status);
	
//	@Query("SELECT b FROM BillingMaster b WHERE b.hospital.id = :hospitalId AND b.moduleType = :moduleType")
//	List<BillingMaster> findBillingByHospitalIdAndModuleType(
//			@Param("hospitalId") Long hospitalId,
//			@Param("moduleType") String moduleType
//			);
	
	
}
