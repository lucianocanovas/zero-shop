package ingsoftware.zeroshop.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.catalog.SubCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, UUID> {

    default Optional<SubCategory> find(UUID id) {
        return findById(id);
    }

    default Optional<SubCategory> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<SubCategory> findByIdAndDeletedFalse(UUID id);
    Optional<SubCategory> findByNameIgnoreCase(String name);
    List<SubCategory> findByCategoryIdAndDeletedFalse(UUID categoryId);
    @org.springframework.data.jpa.repository.Query("SELECT sc FROM SubCategory sc LEFT JOIN FETCH sc.category WHERE sc.deleted = false")
    List<SubCategory> findAllWithCategoryByDeletedFalse();

    List<SubCategory> findAllByDeletedFalse();

}
