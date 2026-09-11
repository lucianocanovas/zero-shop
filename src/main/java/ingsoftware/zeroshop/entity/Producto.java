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
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private String talle;

    @Builder.Default
    @Column(name = "en_oferta", nullable = false)
    private boolean enOferta = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    // Relación con Imagen (1 Producto -> 1..* Imagenes)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "producto_id")
    @Builder.Default
    private List<Imagen> imagenes = new ArrayList<>();
}
