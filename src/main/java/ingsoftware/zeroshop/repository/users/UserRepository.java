package ingsoftware.zeroshop.repository.users;

import ingsoftware.zeroshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> find(UUID id);
    Optional<User> findActive(UUID id);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional <User> findByEmailIgnoreCaseAndDeletedFalse(String email);
    List<User> findAllByDeletedFalse();
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}