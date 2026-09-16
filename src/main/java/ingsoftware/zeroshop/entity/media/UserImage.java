package ingsoftware.zeroshop.entity.media;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.entity.actor.User;

@Entity
@Table(name = "user_images")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserImage extends Image {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

