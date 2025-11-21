//package com.billing.scheduler;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.transaction.annotation.Transactional;
//import com.billing.enums.PaymentStatus;
//import com.billing.model.BillingMaster;
//import com.billing.model.IPDBillingDetails;
//import com.billing.repository.IPDBillingRepository;
//
//public class IPDBillingSchedulerService {
//	
//	
//    @Autowired private IPDBillingRepository ipdBillingRepository;
//	
//	@Scheduled(cron = "0 */2 * * * ?")
//    @Transactional
//    public void runAutoBilling() {
//		List<IPDBillingDetails> request = ipdBillingRepository.findAll();
//		
//		
//		for (IPDBillingDetails ipdBillingDetails : request) {
//			
//			long daysAdmitted = Math.max(1,
//	                ChronoUnit.DAYS.between(request.getAdmissionDate(),
//	                        request.getDischargeDate()) + 1);
//
//
//
//	        double totalBeforeDiscount = roomCharges + medicationCharges + doctorFees + nursingCharges +
//	                diagnosticCharges + procedureCharges + foodCharges + miscCharges;
//
//	        double discountAmount      = totalBeforeDiscount * (request.getDiscountPercentage() / 100);
//	        double totalAfterDiscount  = totalBeforeDiscount - discountAmount;
//
//	        double gstAmount           = totalAfterDiscount * (request.getGstPercentage() / 100);
//	        double finalTotal          = totalAfterDiscount + gstAmount;
//
//	        // ----- BillingMaster -----
//	        BillingMaster billingMaster = new BillingMaster();
//
//	        billingMaster.setTotalAmount(finalTotal);
//	        billingMasterRepository.save(billingMaster);
//
//	        // ----- IPDBillingDetails -----
//	        IPDBillingDetails details = new IPDBillingDetails();
//	        details.setBillingMaster(billingMaster);
//	        details.setAdmissionId(request.getAdmissionId());
//	        details.setRoomCharges(roomCharges);
//	        details.setMedicationCharges(medicationCharges);
//	        details.setDoctorFees(doctorFees);
//	        details.setNursingCharges(nursingCharges);
//	        details.setDiagnosticCharges(diagnosticCharges);
//	        details.setProcedureCharges(procedureCharges);
//	        details.setFoodCharges(foodCharges);
//	        details.setMiscellaneousCharges(miscCharges);
//	        details.setDaysAdmitted(daysAdmitted);
//	        details.setTotalBeforeDiscount(totalBeforeDiscount);
//	        details.setDiscountPercentage(request.getDiscountPercentage());
//	        details.setDiscountAmount(discountAmount);
//	        details.setTotalAfterDiscountAndGst(totalAfterDiscount);
//	        details.setGstPercentage(request.getGstPercentage());
//	        details.setGstAmount(gstAmount);
//	        details.setTotal(finalTotal);
//
//	        return ipdBillingRepository.save(details);
//		}
//}
//
//}
