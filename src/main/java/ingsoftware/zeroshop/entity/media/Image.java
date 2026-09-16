package ingsoftware.zeroshop.entity.media;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.entity.actor.Person;
import ingsoftware.zeroshop.enums.IMGType;

@Entity
@Table (name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "mime_type", nullable = false)
    private String mime_type;
    @Column(name = "content", nullable = false)
    private byte[] content;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private IMGType type;
    
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    @Column (name = "deleted", nullable = false)
    private Boolean deleted;
}
