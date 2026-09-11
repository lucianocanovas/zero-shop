package ingsoftware.zeroshop.repository.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.actor.Client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> find(UUID id);
    Optional<Client> findActive(UUID id);
    Optional<Client> findByIdAndDeletedFalse(UUID id);
    List<Client> findAllByDeletedFalse();

}
