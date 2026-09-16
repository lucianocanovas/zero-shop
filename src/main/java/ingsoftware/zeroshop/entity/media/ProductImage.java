package ingsoftware.zeroshop.entity.media;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.enums.ImageType;

@Entity
@Table (name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
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
    private ImageType type;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;
    @OneToOne
    @JoinColumn(name = "uploaded_by", referencedColumnName = "id")
    private User uploadedBy;

    @Column (name = "deleted", nullable = false)
    private Boolean deleted;
}