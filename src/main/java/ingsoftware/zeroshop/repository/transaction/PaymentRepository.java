package ingsoftware.zeroshop.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.transaction.Payment;
import ingsoftware.zeroshop.enums.PaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    default Optional<Payment> find(UUID id) {
        return findById(id);
    }

    default Optional<Payment> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Payment> findByIdAndDeletedFalse(UUID id);
    List<Payment> findByOrderIdAndDeletedFalse(UUID orderId);
    List<Payment> findByMethodAndDeletedFalse(PaymentMethod method);
    List<Payment> findAllByDeletedFalse();

}

