package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import ingsoftware.zeroshop.enums.EstadoFactura;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_factura", nullable = false, unique = true)
    private Long numeroFactura;

    @Column(name = "fecha_factura", nullable = false)
    private LocalDateTime fechaFactura;

    @Column(name = "total_pagado", nullable = false)
    private Double totalPagado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado;

    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pago_id")
    private FormaDePago formaDePago;

    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "factura_id")
    private List<DetalleFactura> detalles = new ArrayList<>();
}
