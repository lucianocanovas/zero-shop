package ingsoftware.zeroshop.repository.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.location.Country;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {

    default Optional<Country> find(UUID id) {
        return findById(id);
    }

    default Optional<Country> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Country> findByIdAndDeletedFalse(UUID id);
    Optional<Country> findByNameIgnoreCaseAndDeletedFalse(String name);
    Optional<Country> findByCodeIgnoreCaseAndDeletedFalse(String code);
    List<Country> findAllByDeletedFalse();
    boolean existsByNameIgnoreCase(String name);

}
