package com.billing.emergency.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.emergency.dto.AddDoctorVisitRequest;
import com.billing.emergency.dto.AddDoctorVisitResponse;
import com.billing.emergency.dto.AddEmergencyItemsRequest;
import com.billing.emergency.dto.AddEmergencyItemsResponse;
import com.billing.emergency.dto.BillInActiveRequest;
import com.billing.emergency.dto.CloseEmergencyBillResponse;
import com.billing.emergency.dto.EmergencyBillResponse;
import com.billing.emergency.dto.EmergencyBillingItemDto;
import com.billing.emergency.dto.EmergencyDoctorVisitDto;
import com.billing.emergency.dto.EmergencyItemDto;
import com.billing.emergency.dto.GenerateEmergencyBillRequest;
import com.billing.emergency.dto.GetEmergencyBillResponse;
import com.billing.emergency.dto.IsHourly;
import com.billing.emergency.dto.PartialPaymentRequest;
import com.billing.emergency.dto.PartialPaymentResponse;
import com.billing.emergency.dto.PaymentEntryDto;
import com.billing.emergency.dto.PaymentHistoryResponse;
import com.billing.emergency.dto.UpdateEmergencyStayRequest;
import com.billing.emergency.dto.UpdateEmergencyStayResponse;
import com.billing.emergency.repository.EmergencyBillingDetailsRepository;
import com.billing.emergency.repository.EmergencyPaymentHistoryRepository;
import com.billing.emergency.service.EmergencyBillingService;
import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;
import com.billing.model.EmergencyBillingDetails;
import com.billing.model.EmergencyBillingItem;
import com.billing.model.EmergencyDoctorVisit;
import com.billing.model.EmergencyPaymentHistory;
import com.billing.repository.BillingMasterRepository;

import jakarta.transaction.Transactional;

@Service
public class EmergencyBillingServiceImpl implements EmergencyBillingService{
	
	@Autowired
	private BillingMasterRepository billingMasterRepo;
	
	@Autowired
	private EmergencyBillingDetailsRepository emergencyBillingDetailsRepo;
	
	@Autowired
	private EmergencyPaymentHistoryRepository emergencyPaymentHistoryRepo;

	
	private Double nullToZero(Double value) {
        return value != null ? value : 0.0;
    }
	

	
	@Override
	@Transactional
	public EmergencyBillResponse generateEmergencyBill(GenerateEmergencyBillRequest request) {

	    if (request.getEmergencyId() == null) {
	        throw new IllegalArgumentException("emergencyId is required");
	    }

	    if (emergencyBillingDetailsRepo.findByEmergencyId(request.getEmergencyId()).isPresent()) {
	        throw new RuntimeException("Billing already exists for emergencyId: " + request.getEmergencyId());
	    }

	    BillingMaster billingMaster = BillingMaster.builder()
	            .hospitaExternallId(request.getHospitaExternallId())
	            .patientExternalId(request.getPatientExternalId())
	            .emergencyId(request.getEmergencyId())
	            .moduleType("EMERGENCY")
	            .totalAmount(0.0)
	            .paymentStatus(PaymentStatus.PENDING)
	            .paymentMode(request.getPaymentMode())
	            .build();

	    billingMaster = billingMasterRepo.save(billingMaster);

	    EmergencyBillingDetails details = new EmergencyBillingDetails();
	    details.setEmergencyId(request.getEmergencyId());
	    details.setBillingMaster(billingMaster);
	    details.setDoctorVisits(new ArrayList<>()); // Initialize
	    details.setTotalDoctorFees(0.0); // Initialize

	    
	    Long totalHours = request.getTotalHoursAdmitted();
	    if (totalHours == null || totalHours < 1) {
	    	totalHours = 1L;
	    }
	    details.setTotalHoursAdmitted(totalHours);

	    Double advance = request.getAdvancePaid() != null ? request.getAdvancePaid() : 0.0;
	    details.setAdvancePaid(advance);
	    details.setTotalPayment(advance);


	    // === Handle Initial Doctor Fees ===
	    Double initialDoctorFees = request.getDoctorFees();
	    if (initialDoctorFees != null && initialDoctorFees > 0) {
	        EmergencyDoctorVisit initialVisit = EmergencyDoctorVisit.builder()
	                .emergencyBillingDetails(details)
	                .doctorName("Primary Consultant")
	                .feesPerVisit(initialDoctorFees)
	                .visitCount(1)
	                .totalFees(initialDoctorFees)
	                .firstVisitDate(LocalDateTime.now())
	                .lastVisitDate(LocalDateTime.now())
	                .remarks("Initial admission consultation fee")
	                .build();

	        details.getDoctorVisits().add(initialVisit);
	        details.setTotalDoctorFees(initialDoctorFees); // Set the total
	    }

	    // === Calculate totals ===
	    double totalBeforeDiscount = details.getTotalDoctorFees();

	    details.setTotal(totalBeforeDiscount);

	    double discountPercentage = request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0.0;
	    double discountAmount = (totalBeforeDiscount * discountPercentage) / 100.0;

	    details.setDiscountPercentage(discountPercentage);
	    details.setDiscountAmount(discountAmount);

	    double totalAfterDiscount = totalBeforeDiscount - discountAmount;
	    details.setTotalAfterDiscount(totalAfterDiscount);

	    details.setItemGstAmount(0.0);
	    double finalTotal = totalAfterDiscount;
	    details.setTotalAfterDiscountAndGst(finalTotal);

	    details.setDue(finalTotal - advance);

	    details = emergencyBillingDetailsRepo.save(details);

	    billingMaster.setTotalAmount(finalTotal);
	    billingMasterRepo.save(billingMaster);

	    // Response
	    EmergencyBillResponse response = new EmergencyBillResponse();
	    response.setBillingId(billingMaster.getId());
	    response.setEmergencyBillingDetailsId(details.getId());
	    response.setEmergencyId(request.getEmergencyId());

	    response.setTotalChargesBeforeDiscount(totalBeforeDiscount);
	    response.setDiscountPercentage(discountPercentage);
	    response.setDiscountAmount(discountAmount);
	    response.setTotalAfterDiscount(finalTotal);
	    response.setAdvancePaid(advance);
	    response.setDue(details.getDue());
	    response.setDoctorFees(details.getTotalDoctorFees());  // Now from field
	    return response;
	}


	//--------------------------------------------Update Billing-----------------------------------------------------------------//

//	@Override
//	@Transactional
//	public UpdateEmergencyStayResponse updateStayAndRecalculate(UpdateEmergencyStayRequest request) {
//
//	    EmergencyBillingDetails details = emergencyBillingDetailsRepo
//	            .findByEmergencyId(request.getEmergencyId())
//	            .orElseThrow(() -> new RuntimeException("Emergency bill not found for emergencyId: " + request.getEmergencyId()));
//
//	    if ("CLOSED".equalsIgnoreCase(details.getBillingStatus())) {
//	        throw new RuntimeException("Cannot update stay on a closed bill");
//	    }
//
//	    // Only update the totalHoursAdmitted — nothing else changes
//	    Long totalHours = request.getTotalHoursAdmitted();
//	    if (totalHours == null || totalHours < 1) {
//	    	totalHours = 1L;
//	    }
//	    details.setTotalHoursAdmitted(totalHours);
//
//	    // === NO recalculation of monitoring, nursing, or room charges ===
//	    // These stay fixed as set during generateEmergencyBill or manual adjustment
//
//	    // Recalculate Only Hourly Charged Items(isHourly = YES)
//	    List<EmergencyBillingItem> items = details.getItems();
//	    
//	    for(EmergencyBillingItem item : items) {
//	    	if(IsHourly.YES.equals(item.getIsHourly())) {
//	    		
//	    		//Total Amount Calculation
//	    		Double baseAmount = item.getPrice() * item.getQuantity() * totalHours;
//	    		item.setTotalAmount(baseAmount);
//	    		
//	    		//Recalculate GST
//	    		Double gstRate = nullToZero(item.getGstPercentage());
//	    		Double gstAmount = (baseAmount * gstRate)/100;
//	    		item.setGstAmount(BigDecimal.valueOf(gstAmount));
//	    		}
//	    	// isHourly = NO → leave totalAmount and gstAmount unchanged	    	
//	    }
//	    
//	    
//	    // === Recalculate only the totals based on current stored values ===
//	    double itemsTotal = details.getItems().stream()
//	            .mapToDouble(item -> item.getTotalAmount() != null ? item.getTotalAmount() : 0.0)
//	            .sum();
//	    
//	    double totalItemGst = items.stream()
//	            .mapToDouble(i -> i.getGstAmount() != null ? i.getGstAmount().doubleValue() : 0.0)
//	            .sum();
//
//	    details.setItemGstAmount(totalItemGst);
//
//	    // Base total = doctor fees only (others removed)
//	    double baseTotal = nullToZero(details.getTotalDoctorFees());
//
//	    double grandTotalBeforeDiscount = baseTotal + itemsTotal;
//	    details.setTotal(grandTotalBeforeDiscount);
//
//	    // Discount
//	    double discountPercentage = nullToZero(details.getDiscountPercentage());
//	    double discountAmount = (grandTotalBeforeDiscount * discountPercentage) / 100.0;
//	    details.setDiscountAmount(discountAmount);
//
//	    double totalAfterDiscount = grandTotalBeforeDiscount - discountAmount;
//	    details.setTotalAfterDiscount(totalAfterDiscount);
//
//	    // GST + Final
////	    double itemsGst = nullToZero(details.getItemGstAmount());
//	    double finalTotal = totalAfterDiscount + totalItemGst;
//	    details.setTotalAfterDiscountAndGst(finalTotal);
//
//	    // Due
//	    double totalPaid = nullToZero(details.getTotalPayment());
//	    details.setDue(finalTotal - totalPaid);
//
//	    details.setUpdatedAt(LocalDateTime.now());
//	    emergencyBillingDetailsRepo.save(details);
//
//	    // Sync BillingMaster
//	    BillingMaster master = details.getBillingMaster();
//	    master.setTotalAmount(finalTotal);
//	    billingMasterRepo.save(master);
//
//	    // === Response ===
//	    UpdateEmergencyStayResponse response = new UpdateEmergencyStayResponse();
//	    response.setEmergencyId(request.getEmergencyId());
//	    response.setTotalHoursAdmitted(totalHours);
//	    response.setDoctorFees(details.getTotalDoctorFees());
//	    response.setItemsTotal(itemsTotal);
//	    response.setItemsGst(totalItemGst);
//	    response.setDiscountApplied(discountAmount);
//	    response.setFinalBillAmount(finalTotal);
//	    response.setTotalPaid(totalPaid);
//	    response.setDueAmount(details.getDue());
//
//	    return response;
//	}

	@Override
	@Transactional
	public UpdateEmergencyStayResponse updateStayAndRecalculate(UpdateEmergencyStayRequest request) {

	    EmergencyBillingDetails details = emergencyBillingDetailsRepo
	            .findByEmergencyId(request.getEmergencyId())
	            .orElseThrow(() -> new RuntimeException("Emergency bill not found for emergencyId: " + request.getEmergencyId()));

	    if ("CLOSED".equalsIgnoreCase(details.getBillingStatus())) {
	        throw new RuntimeException("Cannot update stay on a closed bill");
	    }

	    // We no longer use totalHoursAdmitted for calculation — only for display if needed
	    // Keep it updated for reference
	    Long totalHours = request.getTotalHoursAdmitted();
	    if (totalHours != null && totalHours >= 1) {
	        details.setTotalHoursAdmitted(totalHours);
	    }

	    LocalDateTime now = LocalDateTime.now();
	    List<EmergencyBillingItem> items = details.getItems();

	    // Recalculate only hourly items based on their individual serviceAddDate
	    for (EmergencyBillingItem item : items) {
	        if (IsHourly.YES.equals(item.getIsHourly())) {
	            LocalDateTime itemStartTime = item.getServiceAddDate();
	            if (itemStartTime == null) {
	                itemStartTime = details.getCreateAt(); // fallback to bill creation time
	            }

	            long elapsedMinutes = ChronoUnit.MINUTES.between(itemStartTime, now);

	            // Ceiling to full hour + minimum 1 hour
	            long billableHoursForItem = (elapsedMinutes > 0) ? ((elapsedMinutes + 59) / 60) : 1L;

	            Double baseAmount = item.getPrice() * item.getQuantity() * billableHoursForItem;
	            item.setTotalAmount(baseAmount);

	            // Recalculate GST
	            Double gstRate = nullToZero(item.getGstPercentage());
	            Double gstAmount = (baseAmount * gstRate) / 100.0;
	            item.setGstAmount(BigDecimal.valueOf(gstAmount));
	        }
	        // isHourly = NO → leave unchanged (one-time charge)
	    }

	    // Recalculate totals from all items
	    double itemsTotal = items.stream()
	            .mapToDouble(i -> nullToZero(i.getTotalAmount()))
	            .sum();

	    double totalItemGst = items.stream()
	            .mapToDouble(i -> i.getGstAmount() != null ? i.getGstAmount().doubleValue() : 0.0)
	            .sum();

	    details.setItemGstAmount(totalItemGst);

	    // Base total = doctor fees only
	    double baseTotal = nullToZero(details.getTotalDoctorFees());

	    double grandTotalBeforeDiscount = baseTotal + itemsTotal;
	    details.setTotal(grandTotalBeforeDiscount);

	    // Discount
	    double discountPercentage = nullToZero(details.getDiscountPercentage());
	    double discountAmount = (grandTotalBeforeDiscount * discountPercentage) / 100.0;
	    details.setDiscountAmount(discountAmount);

	    double totalAfterDiscount = grandTotalBeforeDiscount - discountAmount;
	    details.setTotalAfterDiscount(totalAfterDiscount);

	    // Final total
	    double finalTotal = totalAfterDiscount + totalItemGst;
	    details.setTotalAfterDiscountAndGst(finalTotal);

	    // Due
	    double totalPaid = nullToZero(details.getTotalPayment());
	    details.setDue(finalTotal - totalPaid);

	    details.setUpdatedAt(LocalDateTime.now());
	    emergencyBillingDetailsRepo.save(details);

	    // Sync BillingMaster
	    BillingMaster master = details.getBillingMaster();
	    master.setTotalAmount(finalTotal);
	    billingMasterRepo.save(master);

	    // Response
	    UpdateEmergencyStayResponse response = new UpdateEmergencyStayResponse();
	    response.setEmergencyId(request.getEmergencyId());
	    response.setTotalHoursAdmitted(details.getTotalHoursAdmitted());
	    response.setDoctorFees(details.getTotalDoctorFees());
	    response.setItemsTotal(itemsTotal);
	    response.setItemsGst(totalItemGst);
	    response.setDiscountApplied(discountAmount);
	    response.setFinalBillAmount(finalTotal);
	    response.setTotalPaid(totalPaid);
	    response.setDueAmount(details.getDue());

	    return response;
	}
	
//--------------------------------------------Add Items to Billing-----------------------------------------------------------------//	

	@Override
	@Transactional
	public AddEmergencyItemsResponse addItemsToBill(AddEmergencyItemsRequest request) {

	    EmergencyBillingDetails details = emergencyBillingDetailsRepo
	            .findByEmergencyId(request.getEmergencyId())
	            .orElseThrow(() -> new RuntimeException("Emergency bill not found for emergencyId: " + request.getEmergencyId()));

	    if ("CLOSED".equalsIgnoreCase(details.getBillingStatus())) {
	        throw new RuntimeException("Cannot add items to a closed bill");
	    }

	    double totalAddedBeforeGst = 0.0;
	    double gstAdded = 0.0;
	    int itemsCount = 0;

	    for (EmergencyItemDto itemDto : request.getItems()) {

	        Double itemTotal = itemDto.getPrice() * itemDto.getQuantity();
	        Double gstAmount = (itemTotal * itemDto.getGstPercentage()) / 100.0;

	        totalAddedBeforeGst += itemTotal;
	        gstAdded += gstAmount;
	        itemsCount++;

	        EmergencyBillingItem item = EmergencyBillingItem.builder()
	                .emergencyBillingDetails(details)
	                .serviceName(itemDto.getServiceName())
	                .category(itemDto.getCategory())
	                .price(itemDto.getPrice())
	                .quantity(itemDto.getQuantity())
	                .totalAmount(itemTotal)
	                .gstPercentage(itemDto.getGstPercentage())
	                .gstAmount(BigDecimal.valueOf(gstAmount))
	                .serviceAddDate(LocalDateTime.now())
	                .isHourly(itemDto.getIsHourly())
	                .build();

	        details.getItems().add(item); // Cascade saves
	    }

	    // Save items first
	    emergencyBillingDetailsRepo.save(details);

	    // Update GST
	    double previousGst = details.getItemGstAmount() != null ? details.getItemGstAmount() : 0.0;
	    double newItemGst = previousGst + gstAdded;
	    details.setItemGstAmount(newItemGst);

	    // === Recalculate full bill ===
	    double baseChargesTotal = details.getTotalDoctorFees();

	    // All items total (including newly added)
	    double itemsTotal = details.getItems().stream()
	            .mapToDouble(i -> i.getTotalAmount() != null ? i.getTotalAmount() : 0.0)
	            .sum();

	    double grandTotalBeforeDiscount = baseChargesTotal + itemsTotal;
	    details.setTotal(grandTotalBeforeDiscount);

	    // === CORRECT: Recalculate discount using percentage ===
	    double discountPercentage = details.getDiscountPercentage() != null ? details.getDiscountPercentage() : 0.0;
	    double discountAmount = (grandTotalBeforeDiscount * discountPercentage) / 100.0;
	    details.setDiscountAmount(discountAmount);  // Update to new amount

	    double totalAfterDiscount = grandTotalBeforeDiscount - discountAmount;
	    details.setTotalAfterDiscount(totalAfterDiscount);

	    // Final total with item GST
	    double finalTotal = totalAfterDiscount + newItemGst;
	    details.setTotalAfterDiscountAndGst(finalTotal);

	    // Due
	    double totalPaid = details.getTotalPayment() != null ? details.getTotalPayment() : 0.0;
	    details.setDue(finalTotal - totalPaid);

	    details.setUpdatedAt(LocalDateTime.now());

	    emergencyBillingDetailsRepo.save(details);

	    // Update BillingMaster
	    details.getBillingMaster().setTotalAmount(finalTotal);
	    billingMasterRepo.save(details.getBillingMaster());

	    // Response
	    AddEmergencyItemsResponse response = new AddEmergencyItemsResponse();
	    response.setEmergencyId(request.getEmergencyId());
	    response.setItemsAdded(itemsCount);
	    response.setNewTotalAdded(totalAddedBeforeGst + gstAdded);
	    response.setUpdatedBillTotal(finalTotal);
	    response.setUpdatedDue(details.getDue());

	    return response;
	}

//--------------------------------------------Get Billing-----------------------------------------------------------------//	
	@Override
//	@Transactional(readOnly = true)
	public GetEmergencyBillResponse getEmergencyBillByEmergencyId(Long emergencyId) {

	    EmergencyBillingDetails details = emergencyBillingDetailsRepo
	            .findByEmergencyId(emergencyId)
	            .orElseThrow(() -> new RuntimeException("Emergency bill not found for emergencyId: " + emergencyId));

	    BillingMaster master = details.getBillingMaster();

	    // === Map Items ===
	    List<EmergencyBillingItemDto> itemDtos = details.getItems().stream()
	            .map(item -> {
	                EmergencyBillingItemDto dto = new EmergencyBillingItemDto();
	                dto.setServiceName(item.getServiceName());
	                dto.setCategory(item.getCategory());
	                dto.setPrice(item.getPrice());
	                dto.setQuantity(item.getQuantity());
	                dto.setTotalAmount(item.getTotalAmount());
	                dto.setGstPercentage(item.getGstPercentage());
	                dto.setGstAmount(item.getGstAmount() != null ? item.getGstAmount().doubleValue() : 0.0);
	                dto.setServiceAddDate(item.getServiceAddDate());
	                dto.setIsHourly(item.getIsHourly());
	                return dto;
	            })
	            .sorted((a, b) -> b.getServiceAddDate().compareTo(a.getServiceAddDate())) // Latest first
	            .toList();

	    // === Map Doctor Visits (Breakdown) ===
	    List<EmergencyDoctorVisitDto> doctorVisitDtos = details.getDoctorVisits().stream()
	            .map(visit -> {
	                EmergencyDoctorVisitDto dto = new EmergencyDoctorVisitDto();
	                dto.setDoctorName(visit.getDoctorName());
	                dto.setFeesPerVisit(visit.getFeesPerVisit());
	                dto.setVisitCount(visit.getVisitCount());
	                dto.setTotalFees(visit.getTotalFees());
	                dto.setFirstVisitDate(visit.getFirstVisitDate());
	                dto.setLastVisitDate(visit.getLastVisitDate());
	                dto.setRemarks(visit.getRemarks());
	                return dto;
	            })
	            .sorted((a, b) -> b.getFirstVisitDate().compareTo(a.getFirstVisitDate())) // Earliest first
	            .toList();

	    // === Totals ===
	    double itemsTotal = itemDtos.stream()
	            .mapToDouble(EmergencyBillingItemDto::getTotalAmount)
	            .sum();

	    double baseTotal = details.getTotalDoctorFees();

	    double totalBeforeDiscount = baseTotal + itemsTotal;

	    double discountPercentage = details.getDiscountPercentage() != null ? details.getDiscountPercentage() : 0.0;
	    double discountAmount = (totalBeforeDiscount * discountPercentage) / 100.0;

	    double totalAfterDiscount = totalBeforeDiscount - discountAmount;

	    double itemsGst = details.getItemGstAmount() != null ? details.getItemGstAmount() : 0.0;
	    double finalAmount = totalAfterDiscount + itemsGst;

	    double totalPaid = details.getTotalPayment() != null ? details.getTotalPayment() : 0.0;
	    double due = finalAmount - totalPaid;

	    // === Build Response ===
	    GetEmergencyBillResponse response = new GetEmergencyBillResponse();

	    response.setEmergencyId(emergencyId);
	    response.setBillingId(master.getId());
	    response.setBillingDetailsId(details.getId());

	    response.setBillingDate(master.getBillingDate());
	    response.setBillingStatus(details.getBillingStatus());
	    response.setPaymentStatus(master.getPaymentStatus());

	    response.setPatientExternalId(master.getPatientExternalId());
	    response.setHospitaExternallId(master.getHospitaExternallId());

	    response.setTotalHoursAdmitted(details.getTotalHoursAdmitted());

	    // Doctor Fees – Total and Breakdown
	    response.setTotalDoctorFees(details.getTotalDoctorFees());  // Synced total
	    response.setDoctorVisits(doctorVisitDtos);                  // Full list

	    response.setItems(itemDtos);

	    response.setBaseTotal(baseTotal);
	    response.setItemsTotal(itemsTotal);
	    response.setTotalBeforeDiscount(totalBeforeDiscount);

	    response.setDiscountPercentage(discountPercentage);
	    response.setDiscountAmount(discountAmount);

	    response.setItemsGstAmount(itemsGst);

	    response.setTotalAfterDiscount(totalAfterDiscount);
	    response.setFinalBillAmount(finalAmount);
	    response.setTotalPaid(totalPaid);
	    response.setDueAmount(due);

	    response.setCreatedAt(details.getCreateAt());
	    response.setUpdatedAt(details.getUpdatedAt());

	    response.setMessage("Emergency bill retrieved successfully");

	    return response;
	}

	

//--------------------------------------------Partial Payment-----------------------------------------------------------------//	
	@Override
	@Transactional
	public PartialPaymentResponse recordPartialPayment(PartialPaymentRequest request) {

	    EmergencyBillingDetails details = emergencyBillingDetailsRepo
	            .findByEmergencyId(request.getEmergencyId())
	            .orElseThrow(() -> new RuntimeException("Emergency bill not found for emergencyId: " + request.getEmergencyId()));

	    if ("CLOSED".equalsIgnoreCase(details.getBillingStatus())) {
	        throw new RuntimeException("Cannot record payment on a closed bill");
	    }

	    // Current final bill amount (recalculated for accuracy)
	    double finalBillAmount = details.getTotalAfterDiscountAndGst() != null 
	            ? details.getTotalAfterDiscountAndGst() 
	            : 0.0;

	    double currentTotalPaid = details.getTotalPayment() != null ? details.getTotalPayment() : 0.0;
	    double currentDue = details.getDue() != null ? details.getDue() : finalBillAmount;

	    // Validate payment doesn't exceed due (optional: allow overpayment or restrict)
	    if (request.getAmount() > currentDue && currentDue > 0) {
	        throw new RuntimeException("Payment amount exceeds due amount. Due: " + currentDue);
	    }

	    // Create payment history record
	    EmergencyPaymentHistory paymentHistory = EmergencyPaymentHistory.builder()
	            .emergencyId(request.getEmergencyId())
	            .amount(request.getAmount())
	            .paymentMode(request.getPaymentMode())
	            .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
	            .paidBy(request.getPaidBy())
	            .receiptNo(request.getReceiptNo() != null && !request.getReceiptNo().isBlank() 
	                      ? request.getReceiptNo() 
	                      : generateReceiptNo(details.getId())) // Auto-generate if blank
	            .billingDetails(details)
	            .createdAt(LocalDateTime.now())
	            .build();

	    paymentHistory = emergencyPaymentHistoryRepo.save(paymentHistory);

	    // Update totals in EmergencyBillingDetails
	    double newTotalPaid = currentTotalPaid + request.getAmount();
	    double newDue = finalBillAmount - newTotalPaid;

	    details.setTotalPayment(newTotalPaid);
	    details.setDue(newDue >= 0 ? newDue : 0.0); // Prevent negative due

	    // If fully paid, optionally update payment status
	    if (newDue <= 0.01) { // tolerance for floating point
	        details.getBillingMaster().setPaymentStatus(PaymentStatus.PAID);
	    } else {
	        details.getBillingMaster().setPaymentStatus(PaymentStatus.PARTIAL);
	    }

	    details.setUpdatedAt(LocalDateTime.now());
	    emergencyBillingDetailsRepo.save(details);

	    // Sync BillingMaster
	    BillingMaster master = details.getBillingMaster();
	    master.setTotalAmount(finalBillAmount); // Ensure consistency
	    billingMasterRepo.save(master);

	    // Response
	    PartialPaymentResponse response = new PartialPaymentResponse();
	    response.setEmergencyId(request.getEmergencyId());
	    response.setPaymentHistoryId(paymentHistory.getId());
	    response.setAmountPaid(request.getAmount());
	    response.setPreviousDue(currentDue);
	    response.setNewDue(newDue >= 0 ? newDue : 0.0);
	    response.setTotalPaidSoFar(newTotalPaid);
	    response.setPaymentMode(request.getPaymentMode());
	    response.setPaymentDate(paymentHistory.getPaymentDate());
	    response.setReceiptNo(paymentHistory.getReceiptNo());

	    return response;
	}

	// Helper method for auto-generating receipt number
	private String generateReceiptNo(Long billingDetailsId) {
	    return "REC-EMG-" + billingDetailsId + "-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	}

	
//--------------------------------------------Method to make billing Inactive-----------------------------------------------------------------//	

	@Override
	public CloseEmergencyBillResponse makeBillInactive(BillInActiveRequest request) {
		
		EmergencyBillingDetails details = emergencyBillingDetailsRepo
				.findByEmergencyId(request.getEmergencyId())
				.orElseThrow(()-> new RuntimeException("No Billing Found for Emergency Id: " + request.getEmergencyId()));
		
		if ("CLOSED".equalsIgnoreCase(details.getBillingStatus())) {
	        throw new RuntimeException("Bill is already closed");
	    }
		
		Double dueAmmount = details.getDue();
		
		if(dueAmmount > 0.01) {
			throw new RuntimeException("Due is still pending can not make billing inactive!");
		}
		// === Close the bill ===
	    details.setBillingStatus("CLOSED");
	    
	 // Update BillingMaster
	    BillingMaster master = details.getBillingMaster();
	    master.setPaymentStatus(dueAmmount <= 0.01 ? PaymentStatus.PAID : PaymentStatus.PARTIAL);
//	    master.setTotalAmount(finalBillAmount);
		
	    emergencyBillingDetailsRepo.save(details);
	    billingMasterRepo.save(master);
		
		Double totalPaid = details.getTotalPayment();
		Double finalBillAmount = details.getTotalAfterDiscountAndGst();
		
//		Creating Response
		CloseEmergencyBillResponse response = new CloseEmergencyBillResponse();
		response.setEmergencyId(request.getEmergencyId());
	    response.setFinalBillAmount(finalBillAmount);
	    response.setTotalPaid(totalPaid);
	    response.setDueAmount(dueAmmount >= 0 ? dueAmmount : 0.0);
	    response.setExcessPaid(dueAmmount < 0 ? Math.abs(dueAmmount) : 0.0);
	    response.setClosedAt(LocalDateTime.now());

	    if (dueAmmount < 0) {
	        response.setMessage("Bill closed. Excess payment of ₹" + String.format("%.2f", Math.abs(dueAmmount)) + " – refund to patient");
	    }

	    return response;
	}
	
	
	@Override
//	@Transactional(readOnly = true)
	public PaymentHistoryResponse getPaymentHistory(Long emergencyId) {

	    EmergencyBillingDetails details = emergencyBillingDetailsRepo
	            .findByEmergencyId(emergencyId)
	            .orElseThrow(() -> new RuntimeException("Emergency bill not found for emergencyId: " + emergencyId));

	    BillingMaster master = details.getBillingMaster();

	    PaymentHistoryResponse response = new PaymentHistoryResponse();
	    response.setEmergencyId(emergencyId);

	    // === 1. Initial Advance Paid (from admission) ===
	    Double advance = details.getAdvancePaid() != null ? details.getAdvancePaid() : 0.0;
	    if (advance > 0) {
	        response.setInitialAdvancePaid(advance);
	        response.setAdvancePaidDate(details.getCreateAt()); // When bill was generated
	        response.setAdvancePaymentMode(master.getPaymentMode() != null 
	                ? master.getPaymentMode().name() 
	                : "UNKNOWN");
	    }

	    // === 2. Partial Payments from History ===
	    List<EmergencyPaymentHistory> historyList = emergencyPaymentHistoryRepo
	            .findByEmergencyIdOrderByPaymentDateAsc(emergencyId); // Need this method in repo

	    List<PaymentEntryDto> paymentDtos = historyList.stream()
	            .map(history -> {
	                PaymentEntryDto dto = new PaymentEntryDto();
	                dto.setPaymentId(history.getId());
	                dto.setAmount(history.getAmount());
	                dto.setPaymentMode(history.getPaymentMode());
	                dto.setPaymentDate(history.getPaymentDate());
	                dto.setPaidBy(history.getPaidBy());
	                dto.setReceiptNo(history.getReceiptNo());
	                return dto;
	            })
	            .toList();

	    response.setPartialPayments(paymentDtos);

	    // === 3. Total Paid ===
	    double totalFromPartial = paymentDtos.stream()
	            .mapToDouble(PaymentEntryDto::getAmount)
	            .sum();

	    response.setTotalPaid(advance + totalFromPartial);

	    return response;
	}
	
	
	@Override
	@Transactional
	public AddDoctorVisitResponse addDoctorVisit(AddDoctorVisitRequest request) {

	    EmergencyBillingDetails details = emergencyBillingDetailsRepo
	            .findByEmergencyId(request.getEmergencyId())
	            .orElseThrow(() -> new RuntimeException("Bill not found for emergencyId: " + request.getEmergencyId()));

	    if ("CLOSED".equalsIgnoreCase(details.getBillingStatus())) {
	        throw new RuntimeException("Cannot add doctor visit to closed bill");
	    }

	    String doctorName = request.getDoctorName().trim();

	    // Find existing record for this doctor
	    EmergencyDoctorVisit visitRecord = details.getDoctorVisits().stream()
	            .filter(v -> v.getDoctorName().equalsIgnoreCase(doctorName))
	            .findFirst()
	            .orElse(null);

	    if (visitRecord != null) {
	        // Update existing doctor visit
	        int newCount = visitRecord.getVisitCount() + request.getVisitCount();
	        visitRecord.setVisitCount(newCount);
	        visitRecord.setTotalFees(visitRecord.getFeesPerVisit() * newCount);
	        visitRecord.setLastVisitDate(LocalDateTime.now());
	        visitRecord.setUpdatedAt(LocalDateTime.now());
	    } else {
	        // Create new doctor visit
	        visitRecord = EmergencyDoctorVisit.builder()
	                .emergencyBillingDetails(details)
	                .doctorExternalId(request.getDoctorExternalId())
	                .doctorName(doctorName)
	                .feesPerVisit(request.getFeesPerVisit())
	                .visitCount(request.getVisitCount())
	                .totalFees(request.getFeesPerVisit() * request.getVisitCount())
	                .firstVisitDate(LocalDateTime.now())
	                .lastVisitDate(LocalDateTime.now())
	                .remarks(request.getRemarks())
	                .build();

	        details.getDoctorVisits().add(visitRecord);
	    }

	    // Save first to persist changes
	    emergencyBillingDetailsRepo.save(details);

	    // === Recalculate and sync totalDoctorFees ===
	    double newTotalDoctorFees = details.getDoctorVisits().stream()
	            .mapToDouble(v -> v.getTotalFees() != null ? v.getTotalFees() : 0.0)
	            .sum();

	    details.setTotalDoctorFees(newTotalDoctorFees);

	    // === Full Bill Recalculation ===
	    double itemsTotal = details.getItems().stream()
	            .mapToDouble(i -> i.getTotalAmount() != null ? i.getTotalAmount() : 0.0)
	            .sum();

	    double baseTotal = details.getTotalDoctorFees();

	    double grandTotalBeforeDiscount = baseTotal + itemsTotal;
	    details.setTotal(grandTotalBeforeDiscount);

	    // Reapply discount
	    double discountPercentage = details.getDiscountPercentage() != null ? details.getDiscountPercentage() : 0.0;
	    double discountAmount = (grandTotalBeforeDiscount * discountPercentage) / 100.0;
	    details.setDiscountAmount(discountAmount);

	    double totalAfterDiscount = grandTotalBeforeDiscount - discountAmount;
	    details.setTotalAfterDiscount(totalAfterDiscount);

	    double gst = details.getItemGstAmount() != null ? details.getItemGstAmount() : 0.0;
	    double finalTotal = totalAfterDiscount + gst;
	    details.setTotalAfterDiscountAndGst(finalTotal);

	    double totalPaid = details.getTotalPayment() != null ? details.getTotalPayment() : 0.0;
	    details.setDue(finalTotal - totalPaid);

	    details.setUpdatedAt(LocalDateTime.now());

	    // Save final state
	    emergencyBillingDetailsRepo.save(details);

	    // Sync BillingMaster
	    details.getBillingMaster().setTotalAmount(finalTotal);
	    billingMasterRepo.save(details.getBillingMaster());

	    // === Response ===
	    AddDoctorVisitResponse response = new AddDoctorVisitResponse();
	    response.setEmergencyId(request.getEmergencyId());
	    response.setDoctorName(doctorName);
	    response.setFeesPerVisit(visitRecord.getFeesPerVisit());
	    response.setTotalVisitsForDoctor(visitRecord.getVisitCount());
	    response.setTotalFeesThisDoctor(visitRecord.getTotalFees());
	    response.setGrandTotalDoctorFees(details.getTotalDoctorFees());  // From field
	    response.setUpdatedFinalBill(finalTotal);
	    response.setUpdatedDue(details.getDue());

	    return response;
	}
}
