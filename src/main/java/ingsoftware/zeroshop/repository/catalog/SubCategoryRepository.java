package ingsoftware.zeroshop.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, UUID> {

    Optional<SubCategory> findByIdAndDeletedFalse(UUID id);
    Optional<SubCategory> findByNameIgnoreCase(String name);
    List<SubCategory> findAllByDeletedFalse();
}
