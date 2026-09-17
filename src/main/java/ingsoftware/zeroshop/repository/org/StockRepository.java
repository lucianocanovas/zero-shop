package ingsoftware.zeroshop.repository.org;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.org.Stock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {

    default Optional<Stock> find(UUID id) {
        return findById(id);
    }

    default Optional<Stock> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Stock> findByIdAndDeletedFalse(UUID id);
    Optional<Stock> findByProductIdAndOfficeIdAndDeletedFalse(UUID productId, UUID officeId);
    List<Stock> findByProductIdAndDeletedFalse(UUID productId);
    List<Stock> findByOfficeIdAndDeletedFalse(UUID officeId);
    List<Stock> findAllByDeletedFalse();

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Stock s WHERE s.product.id = :productId AND s.deleted = false")
    Integer getTotalQuantityByProductId(@Param("productId") UUID productId);

}

