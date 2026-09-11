package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.enums.TipoTelefono;

@Entity
@Table(name = "contactos_telefonicos")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactoTelefonico extends Contacto {

    @Column(nullable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_telefono")
    private TipoTelefono tipoTelefono;
}

