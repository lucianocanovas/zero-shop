package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.enums.PhoneType;

@Entity
@Table(name = "contactos_telefonicos")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactPhone extends Contact {

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_telefono")
    private PhoneType phoneType;
}

