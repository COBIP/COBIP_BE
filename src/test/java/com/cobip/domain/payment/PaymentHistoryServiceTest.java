package com.cobip.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.payment.PaymentHistoryResponse;
import com.cobip.global.common.PageResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PaymentHistoryServiceTest {

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    private PaymentHistoryService paymentHistoryService;

    @BeforeEach
    void setUp() {
        paymentHistoryService = new PaymentHistoryService(paymentHistoryRepository);
    }

    @Test
    void getMyPaymentsReturnsUserPaymentHistories() {
        User user = user();
        PaymentHistory paymentHistory = paymentHistory(user);
        when(paymentHistoryRepository.findByUserIdOrderByPaidAtDescIdDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(paymentHistory)));

        PageResponse<PaymentHistoryResponse> response = paymentHistoryService.getMyPayments(user, PageRequest.of(0, 20));

        verify(paymentHistoryRepository).findByUserIdOrderByPaidAtDescIdDesc(1L, PageRequest.of(0, 20));
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getPlanName()).isEqualTo("Pro Monthly");
        assertThat(response.getContent().get(0).getAmount()).isEqualTo(9900);
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    private PaymentHistory paymentHistory(User user) {
        return PaymentHistory.builder()
                .id(1L)
                .user(user)
                .planName("Pro Monthly")
                .amount(9900)
                .currency("KRW")
                .status(PaymentStatus.PAID)
                .paymentMethod("CARD")
                .receiptUrl("https://example.com/receipt")
                .paidAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
