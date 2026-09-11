package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import ingsoftware.zeroshop.enums.ImageType;

@Entity
@Table(name = "imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mime;

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] content;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_imagen", nullable = false)
    private ImageType ImageType;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;
}
