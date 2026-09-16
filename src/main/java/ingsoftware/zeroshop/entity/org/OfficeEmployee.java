package ingsoftware.zeroshop.entity.org;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.entity.actor.Employee;

@Entity
@Table(name = "office_employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

