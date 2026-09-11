package ingsoftware.zeroshop.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.enums.TipoContacto;

@Entity
@Table(name = "contactos")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contacto")
    private TipoContacto tipoContacto;

    private String observacion;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}

