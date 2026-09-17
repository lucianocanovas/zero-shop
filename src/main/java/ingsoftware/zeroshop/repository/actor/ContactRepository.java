package ingsoftware.zeroshop.repository.actor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.actor.Contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    default Optional<Contact> find(UUID id) {
        return findById(id);
    }

    default Optional<Contact> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Contact> findByIdAndDeletedFalse(UUID id);
    List<Contact> findAllByDeletedFalse();

}

