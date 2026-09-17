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
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByUsernameIgnoreCaseAndDeletedFalse(String username);
    List<User> findAllByDeletedFalse();
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

}