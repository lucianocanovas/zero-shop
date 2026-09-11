package ingsoftware.zeroshop.repository.geografic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ingsoftware.zeroshop.entity.State;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {
    Optional<State> findById(UUID uuid);
    Optional<State> findByName(String name);
    List<State> findAllStates();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
