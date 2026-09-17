package ingsoftware.zeroshop.repository.actor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.actor.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    default Optional<User> find(UUID id) {
        return findById(id);
    }

    default Optional<User> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<User> findByIdAndDeletedFalse(UUID id);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmailIgnoreCaseAndDeletedFalse(String email);
    List<User> findAllByDeletedFalse();
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

}