package ingsoftware.zeroshop.entity.transaction;

import ingsoftware.zeroshop.entity.actor.Proveedor;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facturas_proveedor")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FacturaProveedor extends Factura {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;
}
