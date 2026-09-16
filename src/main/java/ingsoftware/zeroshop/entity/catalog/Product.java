package ingsoftware.zeroshop.entity.catalog;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.Size;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false)
    private Size size;

    @Builder.Default
    @Column(name = "on_sale", nullable = false)
    private Boolean onSale = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", referencedColumnName = "id")
    private SubCategory subCategory;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}