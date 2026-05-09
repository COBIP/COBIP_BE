package com.cobip.dto.payment;

import java.time.LocalDateTime;

import com.cobip.domain.payment.PaymentHistory;
import com.cobip.domain.payment.PaymentStatus;

import lombok.Getter;

@Getter
public class PaymentHistoryResponse {

    private final Long id;
    private final String planName;
    private final int amount;
    private final String currency;
    private final PaymentStatus status;
    private final String paymentMethod;
    private final String receiptUrl;
    private final LocalDateTime paidAt;

    private PaymentHistoryResponse(PaymentHistory paymentHistory) {
        this.id = paymentHistory.getId();
        this.planName = paymentHistory.getPlanName();
        this.amount = paymentHistory.getAmount();
        this.currency = paymentHistory.getCurrency();
        this.status = paymentHistory.getStatus();
        this.paymentMethod = paymentHistory.getPaymentMethod();
        this.receiptUrl = paymentHistory.getReceiptUrl();
        this.paidAt = paymentHistory.getPaidAt();
    }

    public static PaymentHistoryResponse from(PaymentHistory paymentHistory) {
        return new PaymentHistoryResponse(paymentHistory);
    }
}
