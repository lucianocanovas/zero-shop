package ingsoftware.zeroshop.entity.transaction;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import ingsoftware.zeroshop.enums.TipoPago;

@Entity
@Table(name = "formas_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormaDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false)
    private TipoPago tipoPago;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}
