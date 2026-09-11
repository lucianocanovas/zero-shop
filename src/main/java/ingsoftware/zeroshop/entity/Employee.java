package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import ingsoftware.zeroshop.enums.EmployeeType;

@Entity
@Table(name = "empleados")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends Person {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empleado", nullable = false)
    private EmployeeType employeeType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Company company;
}
