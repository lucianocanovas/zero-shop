package ingsoftware.zeroshop.repository.org;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.org.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    default Optional<Organization> find(UUID id) {
        return findById(id);
    }

    default Optional<Organization> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Organization> findByIdAndDeletedFalse(UUID id);
    Optional<Organization> findByNameIgnoreCaseAndDeletedFalse(String name);
    List<Organization> findAllByDeletedFalse();

}

