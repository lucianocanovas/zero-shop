package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Size size;

    @Builder.Default
    @Column(name = "en_oferta", nullable = false)
    private boolean discount = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    // Relación con Imagen (1 Producto -> 1..* Imagenes)
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "producto_id")
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    // Relación con SubCategoria (* Productos -> 1 SubCategoria)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategoria_id")
    private SubCategory subCategory;
}
