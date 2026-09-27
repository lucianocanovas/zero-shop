package ingsoftware.zeroshop.entity.catalog;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Column(name = "image_url")
    private String imageUrl;

    @Builder.Default
    @Column(name = "on_sale", nullable = false)
    private Boolean onSale = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", referencedColumnName = "id")
    private SubCategory subCategory;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Transient
    private BigDecimal currentPrice;

    @Transient
    private Integer stock;

    public Category getCategory() {
        return subCategory != null ? subCategory.getCategory() : null;
    }

    @Transient
    public List<Map<String, String>> getImages() {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return List.of(Map.of("url", imageUrl));
        }
        return Collections.emptyList();
    }
    
}