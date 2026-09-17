package ingsoftware.zeroshop.repository.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.media.ProductImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    default Optional<ProductImage> find(UUID id) {
        return findById(id);
    }

    default Optional<ProductImage> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<ProductImage> findByIdAndDeletedFalse(UUID id);
    List<ProductImage> findByProductIdAndDeletedFalse(UUID productId);
    List<ProductImage> findAllByDeletedFalse();

}

