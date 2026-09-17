package ingsoftware.zeroshop.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.enums.Size;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    default Optional<Product> find(UUID id) {
        return findById(id);
    }

    default Optional<Product> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Product> findByIdAndDeletedFalse(UUID id);
    Optional<Product> findByNameAndDeletedFalse(String name);
    Optional<Product> findByCodeAndDeletedFalse(String code);
    List<Product> findAllByDeletedFalse();
    List<Product> findBySizeAndDeletedFalse(Size size);
    List<Product> findByOnSaleTrueAndDeletedFalse();
    List<Product> findByOnSaleAndDeletedFalse(Boolean onSale);
    List<Product> findBySubCategoryIdAndDeletedFalse(UUID subCategoryId);
    List<Product> findBySubCategoryCategoryNameAndDeletedFalse(String categoryName);

}
