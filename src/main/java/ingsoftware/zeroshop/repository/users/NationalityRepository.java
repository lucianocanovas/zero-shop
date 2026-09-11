package ingsoftware.zeroshop.repository.users;


import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ingsoftware.zeroshop.entity.Nationality;

import java.util.List;
import java.util.Optional;

@Repository
public interface NationalityRepository extends JpaRepository<Nationality, UUID> {

    Optional<Nationality> findByIdAndDeletedFalse(UUID id);
    Optional<Nationality> findByNameIgnoreCase(String name);
    List<Nationality> findAllByDeletedFalse();

}
