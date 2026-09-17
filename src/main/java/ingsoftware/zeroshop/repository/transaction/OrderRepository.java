package ingsoftware.zeroshop.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.transaction.Order;
import ingsoftware.zeroshop.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    default Optional<Order> find(UUID id) {
        return findById(id);
    }

    default Optional<Order> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Order> findByIdAndDeletedFalse(UUID id);
    List<Order> findByStatusAndDeletedFalse(OrderStatus status);
    List<Order> findByDateBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end);
    List<Order> findAllByDeletedFalse();

}

