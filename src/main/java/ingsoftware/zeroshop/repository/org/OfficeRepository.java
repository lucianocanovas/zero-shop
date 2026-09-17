package ingsoftware.zeroshop.repository.org;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.org.Office;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfficeRepository extends JpaRepository<Office, UUID> {

    default Optional<Office> find(UUID id) {
        return findById(id);
    }

    default Optional<Office> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Office> findByIdAndDeletedFalse(UUID id);
    Optional<Office> findByNameIgnoreCaseAndDeletedFalse(String name);
    Optional<Office> findByCuitAndDeletedFalse(String cuit);
    List<Office> findAllByDeletedFalse();
    boolean existsByCuitAndDeletedFalse(String cuit);

}

