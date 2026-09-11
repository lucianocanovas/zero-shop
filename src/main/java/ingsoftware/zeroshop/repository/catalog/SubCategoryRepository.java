package ingsoftware.zeroshop.repository.catalog;

import ingsoftware.zeroshop.entity.SubCategory;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, UUID> {
    Optional<SubCategory> find(UUID id);
    Optional<SubCategory> findActive(UUID id);
    Optional<SubCategory> findByIdAndDeletedFalse(UUID id);
    Optional<SubCategory> findByNameIgnoreCase(String name);
    List<SubCategory> findAllByDeletedFalse();
}
