package ingsoftware.zeroshop.repository.actor;

import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.enums.EmployeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    default Optional<Employee> find(UUID id) {
        return findById(id);
    }

    default Optional<Employee> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Employee> findByIdAndDeletedFalse(UUID id);
    List<Employee> findAllByDeletedFalse();
    List<Employee> findByEmployeeTypeAndDeletedFalse(EmployeeType employeeType);

}
