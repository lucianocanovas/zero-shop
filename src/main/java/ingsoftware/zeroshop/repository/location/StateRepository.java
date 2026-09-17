package ingsoftware.zeroshop.repository.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.location.State;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {

    default Optional<State> find(UUID id) {
        return findById(id);
    }

    default Optional<State> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<State> findByIdAndDeletedFalse(UUID id);
    Optional<State> findByNameIgnoreCaseAndDeletedFalse(String name);
    List<State> findByCountryIdAndDeletedFalse(UUID countryId);
    List<State> findAllByDeletedFalse();
    boolean existsByNameIgnoreCase(String name);

}
