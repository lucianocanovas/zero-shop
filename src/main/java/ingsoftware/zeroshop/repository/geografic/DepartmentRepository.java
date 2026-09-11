package ingsoftware.zeroshop.repository.geografic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ingsoftware.zeroshop.entity.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findById(UUID uuid);
    Optional<Department> findByName(String name);
    List<Department> findAllDepartments();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

}
