package ingsoftware.zeroshop.repository.actor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.actor.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    default Optional<Supplier> find(UUID id) {
        return findById(id);
    }

    default Optional<Supplier> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Supplier> findByIdAndDeletedFalse(UUID id);
    Optional<Supplier> findByNameIgnoreCaseAndDeletedFalse(String name);
    List<Supplier> findAllByDeletedFalse();
    boolean existsByNameIgnoreCase(String name);

}

