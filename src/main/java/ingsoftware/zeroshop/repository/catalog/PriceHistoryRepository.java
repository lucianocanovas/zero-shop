package ingsoftware.zeroshop.repository.catalog;

import ingsoftware.zeroshop.entity.PriceHistory;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {
    Optional<PriceHistory> find(UUID id);
    Optional<PriceHistory> findActive(UUID id);
    Optional<PriceHistory> findByIdAndProductId(UUID id, UUID productId);
    List<PriceHistory> findAllByProductId(UUID productId);
    List<PriceHistory> findByDateBetweenAndProductId(java.util.Date startDate, java.util.Date endDate, UUID productId);
}
