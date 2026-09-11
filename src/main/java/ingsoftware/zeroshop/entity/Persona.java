package ingsoftware.zeroshop.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.TipoDocumento;

@Entity
@Table(name = "personas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;


    // Relación con Direccion (1..*) Varias direcciones.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)// Cascade.all sirve para que cuando se elimine la persona, se eliminen sus direcciones asociadas.
    @JoinColumn(name = "persona_id")
    @Builder.Default
    private List<Direccion> direcciones = new ArrayList<>();

    // Composición con Usuario (1 Persona -> 1..* Usuarios)
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<User> users = new ArrayList<>();

    // Relación con Contacto (1..*) Puede tener un email y un telefono
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id")
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();

    // Relación con Imagen (1 Persona -> 1..* Imagenes)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id")
    @Builder.Default
    private List<Imagen> imagenes = new ArrayList<>();
}
