package com.cobip.web.payment;

import com.cobip.domain.payment.PaymentHistoryService;
import com.cobip.domain.user.User;
import com.cobip.dto.payment.PaymentHistoryResponse;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/payments")
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentHistoryResponse>>> getMyPayments(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentHistoryService.getMyPayments(user, pageable)));
    }
}
