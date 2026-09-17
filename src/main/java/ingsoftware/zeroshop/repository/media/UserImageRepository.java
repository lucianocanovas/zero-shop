package ingsoftware.zeroshop.repository.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ingsoftware.zeroshop.entity.media.UserImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, UUID> {

    default Optional<UserImage> find(UUID id) {
        return findById(id);
    }

    default Optional<UserImage> findActive(UUID id) {
        return findByIdAndDeletedFalse(id);
    }

    Optional<UserImage> findByIdAndDeletedFalse(UUID id);
    Optional<UserImage> findByUserIdAndDeletedFalse(UUID userId);
    List<UserImage> findAllByDeletedFalse();

}

