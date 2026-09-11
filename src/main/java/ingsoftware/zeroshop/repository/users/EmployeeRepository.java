package ingsoftware.zeroshop.repository.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ingsoftware.zeroshop.entity.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByIdAndDeletedFalse(UUID id);
    List<Employee> findAllByDeletedFalse();
    List<Employee> findByEmployeeTypeAndDeletedFalse(EmployeeType employeeType);

}
