package ingsoftware.zeroshop.repository.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.media.Image;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {

    default Optional<Image> find(UUID id) {
        return findById(id);
    }

    default Optional<Image> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<Image> findByIdAndDeletedFalse(UUID id);
    List<Image> findAllByDeletedFalse();

}

