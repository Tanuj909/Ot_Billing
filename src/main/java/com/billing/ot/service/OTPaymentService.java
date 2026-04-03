package com.billing.ot.service;

import com.billing.ot.dto.OTAdvancePaymentRequest;
import com.billing.ot.dto.OTPaymentHistoryResponse;
import com.billing.ot.dto.OTPaymentRequest;
import com.billing.ot.dto.OTPaymentResponse;
import com.billing.ot.dto.OTRefundRequest;
import com.billing.ot.dto.OTRefundResponse;

public interface OTPaymentService {

	OTPaymentResponse makePayment(OTPaymentRequest request);

	OTRefundResponse initiateRefund(OTRefundRequest request);

	OTRefundResponse completeRefund(Long refundId);

	OTPaymentHistoryResponse getPaymentHistory(Long operationId);

	OTPaymentResponse makeAdvancePayment(OTAdvancePaymentRequest request);

}
