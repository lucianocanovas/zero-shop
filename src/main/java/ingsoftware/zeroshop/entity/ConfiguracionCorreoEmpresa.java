package ingsoftware.zeroshop.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuraciones_correo_empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCorreoEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String clave;

    @Column(nullable = false)
    private String puerto;

    @Column(nullable = false)
    private String smtp;

    @Column(nullable = false)
    private boolean tls;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}

