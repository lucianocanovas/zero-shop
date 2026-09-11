package ingsoftware.zeroshop.repository.users;

import org.hibernate.validator.constraints.UUID;
import org.springframework.stereotype.Repository;
import ingsoftware.zeroshop.entity.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    Optional<Person> findByIdAndDeletedFalse(UUID id);
    Optional<Person> findByDocumentTypeAndDocumentNumberAndDeletedFalseIgnoreCase(DocumentType documentType, String documentNumber);
    List <Person> findByNameIgnoreCaseAndDeletedFalse(String name);
    List <Person> findBySurnameIgnoreCaseAndDeletedFalse(String surname);
    List <Person> findByNameIgnoreCaseAndSurnameIgnoreCaseAndDeletedFalse(String name, String surname);
    List <Person> findAllByDeletedFalse();

}
