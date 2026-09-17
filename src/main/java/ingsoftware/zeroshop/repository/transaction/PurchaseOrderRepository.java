package ingsoftware.zeroshop.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.transaction.PurchaseOrder;
import ingsoftware.zeroshop.enums.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    default Optional<PurchaseOrder> find(UUID id) {
        return findById(id);
    }

    default Optional<PurchaseOrder> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<PurchaseOrder> findByIdAndDeletedFalse(UUID id);
    List<PurchaseOrder> findBySupplierIdAndDeletedFalse(UUID supplierId);
    List<PurchaseOrder> findByStatusAndDeletedFalse(OrderStatus status);
    List<PurchaseOrder> findByOfficeIdAndDeletedFalse(UUID officeId);
    List<PurchaseOrder> findAllByDeletedFalse();

}

