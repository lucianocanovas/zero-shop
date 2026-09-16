package ingsoftware.zeroshop.entity.media;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.ImageType;

@Entity
@Table(name = "images")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "mime_type", nullable = false)
    private String mimeType;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", nullable = false)
    private byte[] content;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ImageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

