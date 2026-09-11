package ingsoftware.zeroshop.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.TipoEmpresa;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false, unique = true)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empresa")
    private TipoEmpresa tipoEmpresa;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    // Una empresa tiene sí o sí una dirección (1..1 obligatoria)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;

    // Una empresa tiene sí o sí una configuración de correo (1..1 obligatoria)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "configuracion_correo_id", nullable = false)
    private ConfiguracionCorreoEmpresa configuracionCorreo;

    // Relación con Contacto (1..*)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "empresa_id")
    @Builder.Default
    private List<Contacto> contactos = new ArrayList<>();

    // Relación con Empleado (1 Empresa -> 1..* Empleados)
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Empleado> empleados = new ArrayList<>();
}

