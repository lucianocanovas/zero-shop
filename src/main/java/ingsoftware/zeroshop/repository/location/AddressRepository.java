package ingsoftware.zeroshop.repository.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.location.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    default Optional<Address> find(UUID id) {
        return findById(id);
    }

    default Optional<Address> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Address> findByIdAndDeletedFalse(UUID id);
    List<Address> findByCityIdAndDeletedFalse(UUID cityId);
    List<Address> findAllByDeletedFalse();

}

