package ingsoftware.zeroshop.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.transaction.OrderDetail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {

    default Optional<OrderDetail> find(UUID id) {
        return findById(id);
    }

    default Optional<OrderDetail> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<OrderDetail> findByIdAndDeletedFalse(UUID id);
    List<OrderDetail> findByOrderIdAndDeletedFalse(UUID orderId);
    Optional<OrderDetail> findByOrderIdAndProductIdAndDeletedFalse(UUID orderId, UUID productId);
    List<OrderDetail> findByProductIdAndDeletedFalse(UUID productId);
    List<OrderDetail> findAllByDeletedFalse();

}

