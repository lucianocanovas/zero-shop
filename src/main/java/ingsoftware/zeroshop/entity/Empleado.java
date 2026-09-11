package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import ingsoftware.zeroshop.enums.TipoEmpleado;

@Entity
@Table(name = "empleados")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Empleado extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empleado", nullable = false)
    private TipoEmpleado tipoEmpleado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}
