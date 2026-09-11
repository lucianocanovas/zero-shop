package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import ingsoftware.zeroshop.enums.EstadoOrdenCompra;

@Entity
@Table(name = "ordenes_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identificador_compra", nullable = false, unique = true)
    private String identificadorCompra;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_orden_compra", nullable = false)
    private EstadoOrdenCompra estadoOrdenCompra;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orden_compra_id")
    @Builder.Default
    private List<DetalleCompra> detalles = new ArrayList<>();
}
