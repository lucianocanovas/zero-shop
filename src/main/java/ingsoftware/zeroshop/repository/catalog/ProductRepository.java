package ingsoftware.zeroshop.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.catalog.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> find(UUID id);
    Optional<Product> findActive(UUID id);
    Optional<Product> findByIdAndDeletedFalse(UUID id);
    Optional<Product> findByNameAndDeletedFalse(String name);
    Optional<Product> findByCodeAndDeletedFalse(String code);
    List<Product> findAllByDeletedFalse();
    List<Product> findBySizeAndDeletedFalse(Size size);
    List<Product> findByDiscountAndDeletedFalse(boolean discount);
    List <Product> findBySubCategoryAndDeletedFalse(String subCategoryName);
    List <Product> findBySubCategoryCategoryNameAndDeletedFalse(String categoryName);
}
