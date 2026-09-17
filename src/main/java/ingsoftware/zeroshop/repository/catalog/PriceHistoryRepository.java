package ingsoftware.zeroshop.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.catalog.PriceHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {

    default Optional<PriceHistory> find(UUID id) {
        return findById(id);
    }

    default Optional<PriceHistory> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<PriceHistory> findByIdAndDeletedFalse(UUID id);
    Optional<PriceHistory> findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(UUID productId);
    List<PriceHistory> findByProductIdAndDeletedFalseOrderByStartDateDesc(UUID productId);
    List<PriceHistory> findByProductIdAndDeletedFalse(UUID productId);
    List<PriceHistory> findByStartDateBetweenAndProductIdAndDeletedFalse(LocalDateTime startDate, LocalDateTime endDate, UUID productId);
    List<PriceHistory> findAllByDeletedFalse();

}
