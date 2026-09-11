package ingsoftware.zeroshop.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.TipoDocumento;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "personas")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    
    @Column(nullable = false)
    private boolean eliminado = false;


    // Relación con Direccion (1..*) Varias direcciones.
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)// Cascade.all sirve para que cuando se elimine la persona, se eliminen sus direcciones asociadas.
    @JoinColumn(name = "persona_id")
    
    private List<Direccion> direcciones = new ArrayList<>();

    // Composición con Usuario (1 Persona -> 1..* Usuarios)
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    
    private List<User> users = new ArrayList<>();

    // Puede tener un email y un telefono
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id")
    
    private List<Contacto> contactos = new ArrayList<>();

    // Relación con Imagen (1 Persona -> 1..* Imagenes)
    //JPA configura OneToMany automaticamente en Lazy, para que asi, no se haga un left join gigante sobre la relacion, y se llene la memoria
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id")
    
    private List<Imagen> imagenes = new ArrayList<>();
}
