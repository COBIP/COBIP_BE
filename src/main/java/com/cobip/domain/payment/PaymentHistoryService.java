package com.cobip.domain.payment;

import com.cobip.domain.user.User;
import com.cobip.dto.payment.PaymentHistoryResponse;
import com.cobip.global.common.PageResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<PaymentHistoryResponse> getMyPayments(User user, Pageable pageable) {
        return PageResponse.from(paymentHistoryRepository
                .findByUserIdOrderByPaidAtDescIdDesc(user.getId(), pageable)
                .map(PaymentHistoryResponse::from));
    }
}
