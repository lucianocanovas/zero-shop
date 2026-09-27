package ingsoftware.zeroshop.repository.actor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.actor.Person;
import ingsoftware.zeroshop.enums.IDType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    default Optional<Person> find(UUID id) {
        return findById(id);
    }

    default Optional<Person> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Person> findByIdAndDeletedFalse(UUID id);
    Optional<Person> findByIdNumber(String idNumber);
    Optional<Person> findByIdNumberAndDeletedFalse(String idNumber);
    Optional<Person> findByIdTypeAndIdNumberAndDeletedFalse(IDType idType, String idNumber);
    List<Person> findByFirstNameIgnoreCaseAndDeletedFalse(String firstName);
    List<Person> findByLastNameIgnoreCaseAndDeletedFalse(String lastName);
    List<Person> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDeletedFalse(String firstName, String lastName);
    List<Person> findAllByDeletedFalse();
    boolean existsByIdNumber(String idNumber);
    boolean existsByIdNumberAndDeletedFalse(String idNumber);
    boolean existsByIdNumberAndDeletedFalseAndIdNot(String idNumber, UUID id);
    boolean existsByIdNumberAndIdNot(String idNumber, UUID id);

}
