package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ingsoftware.zeroshop.enums.ContactType;

@Entity
@Table(name = "contactos")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contacto")
    private ContactType contactType;

    private String observation;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;
}

