package ingsoftware.zeroshop.entity.org;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.CompanyType;
import ingsoftware.zeroshop.entity.actor.Contact;
import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.entity.location.Address;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "razon_social", nullable = false)
    private String SocialReason;

    @Column(nullable = false, unique = true)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empresa")
    private CompanyType companyType;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    // Una empresa tiene sí o sí una dirección (1..1 obligatoria)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "direccion_id", nullable = false)
    private Address address;

    // Una empresa tiene sí o sí una configuración de correo (1..1 obligatoria)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "configuracion_correo_id", nullable = false)
    private CompanyMailSetUp companyMailSetUp;

    // Relación con Contacto (1..*)
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "empresa_id")
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();

    // Relación con Empleado (1 Empresa -> 1..* Empleados)
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
}

