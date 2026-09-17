package ingsoftware.zeroshop.repository.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.location.City;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    default Optional<City> find(UUID id) {
        return findById(id);
    }

    default Optional<City> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<City> findByIdAndDeletedFalse(UUID id);
    Optional<City> findByNameIgnoreCaseAndDeletedFalse(String name);
    List<City> findByStateIdAndDeletedFalse(UUID stateId);
    List<City> findAllByDeletedFalse();
    boolean existsByNameIgnoreCase(String name);

}
