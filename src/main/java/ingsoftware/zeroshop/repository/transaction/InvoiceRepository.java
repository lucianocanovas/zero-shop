package ingsoftware.zeroshop.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.transaction.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    default Optional<Invoice> find(UUID id) {
        return findById(id);
    }

    default Optional<Invoice> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Invoice> findByIdAndDeletedFalse(UUID id);
    Optional<Invoice> findByNumberAndDeletedFalse(String number);
    Optional<Invoice> findByOrderIdAndDeletedFalse(UUID orderId);
    List<Invoice> findAllByDeletedFalse();

}

