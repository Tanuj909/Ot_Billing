package com.billing.icu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.icu.entity.ICUPayment;

public interface ICUPaymentRepository
extends JpaRepository<ICUPayment, Long> {

List<ICUPayment> findByIcuBillingDetailsIdOrderByPaidAtDesc(
    Long icuBillingId
);
}