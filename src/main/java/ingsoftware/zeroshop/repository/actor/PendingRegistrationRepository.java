package ingsoftware.zeroshop.repository.actor;

import ingsoftware.zeroshop.entity.actor.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, UUID> {
    Optional<PendingRegistration> findByEmailIgnoreCase(String email);
    void deleteByEmailIgnoreCase(String email);
}
