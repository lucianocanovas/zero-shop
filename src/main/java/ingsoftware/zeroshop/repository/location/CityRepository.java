package ingsoftware.zeroshop.repository.geografic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ingsoftware.zeroshop.entity.City;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> find(UUID id);
    Optional<City> findActive(UUID id);
    Optional<City> findById(UUID uuid);
    Optional<City> findByName(String name);
    List<City> findAllCities();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

}
