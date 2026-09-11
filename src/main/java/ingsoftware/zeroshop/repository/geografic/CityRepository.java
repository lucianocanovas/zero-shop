package ingsoftware.zeroshop.repository.geografic;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ingsoftware.zeroshop.entity.City;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    Optional<City> findById(UUID uuid);
    Optional<City> findByName(String name);
    List<City> findAllCities();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

}
