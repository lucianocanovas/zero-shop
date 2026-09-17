package ingsoftware.zeroshop.repository.actor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.actor.Client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    default Optional<Client> find(UUID id) {
        return findById(id);
    }

    default Optional<Client> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Client> findByIdAndDeletedFalse(UUID id);
    List<Client> findAllByDeletedFalse();

}
