package ingsoftware.zeroshop.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.catalog.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    default Optional<Category> find(UUID id) {
        return findById(id);
    }

    default Optional<Category> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Category> findByIdAndDeletedFalse(UUID id);
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findAllByDeletedFalse();

}
