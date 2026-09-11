package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facturas_cliente")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FacturaCliente extends Factura {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", unique = true, nullable = false)
    private OrdenCompra ordenCompra;
}
