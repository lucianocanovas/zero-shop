package ingsoftware.zeroshop.service.transaction;

import ingsoftware.zeroshop.entity.transaction.Order;
import ingsoftware.zeroshop.entity.transaction.Payment;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.transaction.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getPaymentsByOrder(UUID orderId) {
        return paymentRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public Optional<Payment> getPaymentById(UUID paymentId) {
        return paymentRepository.findActive(paymentId);
    }

    @Transactional
    public Payment registerPayment(Order order, BigDecimal amount, PaymentMethod method) {
        Payment payment = Payment.builder()
                .order(order)
                .amount(amount != null ? amount : BigDecimal.ZERO)
                .date(LocalDateTime.now())
                .method(method)
                .deleted(false)
                .build();
        return paymentRepository.save(payment);
    }
}
