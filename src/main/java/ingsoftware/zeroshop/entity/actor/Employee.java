package ingsoftware.zeroshop.entity.actor;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.EmployeeType;

@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends Person {

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false)
    private EmployeeType employeeType;

    @Column(name = "hire_date")
    private LocalDate hireDate;

}