package ingsoftware.zeroshop.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vigencias_precio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime dateFrom;

    @Column(name = "fecha_hasta")
    private LocalDateTime dateTo;

    @Column(nullable = false)
    private Double price;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    // NOTA: SOLUCION AL PROBLEMA DE LA COLECCIÓN EN PRODUCTO
    // Para evitar cargar todas las vigencias de precios directamente desde Producto, 
    // asociamos el ID del producto aquí usando @ManyToOne sin armar la @OneToMany del otro lado.
    // Además, usamos @OnDelete de Hibernate para que si el producto se elimina en base de datos, 
    // se borren en cascada las vigencias sin que JPA tenga que cargarlas en memoria.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;
}
