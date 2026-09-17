package ingsoftware.zeroshop.repository.actor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.actor.SupplierProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierProductRepository extends JpaRepository<SupplierProduct, UUID> {

    default Optional<SupplierProduct> find(UUID id) {
        return findById(id);
    }

    default Optional<SupplierProduct> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<SupplierProduct> findByIdAndDeletedFalse(UUID id);
    List<SupplierProduct> findByProductIdAndDeletedFalse(UUID productId);
    List<SupplierProduct> findBySupplierIdAndDeletedFalse(UUID supplierId);
    Optional<SupplierProduct> findBySupplierIdAndProductIdAndDeletedFalse(UUID supplierId, UUID productId);
    List<SupplierProduct> findByProductIdAndDeletedFalseOrderByCostPriceAsc(UUID productId);
    List<SupplierProduct> findAllByDeletedFalse();

}

