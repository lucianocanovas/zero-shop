package ingsoftware.zeroshop.repository.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ingsoftware.zeroshop.entity.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByIdAndDeletedFalse(UUID id);
    List<Customer> findAllByDeletedFalse();

}
