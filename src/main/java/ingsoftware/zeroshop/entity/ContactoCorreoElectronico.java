package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contactos_correos_electronicos")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactoCorreoElectronico extends Contacto {

    @Column(nullable = false)
    private String email;
}

