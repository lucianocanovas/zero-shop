package ingsoftware.zeroshop.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, UUID> {

    default Optional<SaleOrder> find(UUID id) {
        return findById(id);
    }

    default Optional<SaleOrder> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<SaleOrder> findByIdAndDeletedFalse(UUID id);
    Optional<SaleOrder> findByClientIdAndStatusAndDeletedFalse(UUID clientId, OrderStatus status);
    List<SaleOrder> findByClientIdAndDeletedFalseOrderByDateDesc(UUID clientId);
    List<SaleOrder> findByClientIdAndStatusNotAndDeletedFalseOrderByDateDesc(UUID clientId, OrderStatus status);
    List<SaleOrder> findByStatusAndDeletedFalse(OrderStatus status);
    List<SaleOrder> findByStatusNotAndDeletedFalseOrderByDateDesc(OrderStatus status);
    List<SaleOrder> findByDateBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end);
    List<SaleOrder> findByOfficeIdAndDeletedFalse(UUID officeId);
    List<SaleOrder> findAllByDeletedFalse();

}

