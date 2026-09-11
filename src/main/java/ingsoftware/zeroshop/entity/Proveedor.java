package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    // Relación con Contacto (1 Proveedor -> 1..* Contactos)
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "proveedor_id")
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();
}
