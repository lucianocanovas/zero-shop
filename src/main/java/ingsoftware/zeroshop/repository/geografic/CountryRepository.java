package ingsoftware.zeroshop.repository.geografic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ingsoftware.zeroshop.entity.Country;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {

    Optional<Country> findById(UUID uuid);
    Optional<Country> findByName(String name);
    List<Country> findAllCountries();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

}
