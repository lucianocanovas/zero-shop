package ingsoftware.zeroshop.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.transaction.InvoiceDetail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, UUID> {

    default Optional<InvoiceDetail> find(UUID id) {
        return findById(id);
    }

    default Optional<InvoiceDetail> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<InvoiceDetail> findByIdAndDeletedFalse(UUID id);
    List<InvoiceDetail> findByInvoiceIdAndDeletedFalse(UUID invoiceId);
    List<InvoiceDetail> findAllByDeletedFalse();

}

