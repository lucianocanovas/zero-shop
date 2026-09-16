package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ingsoftware.zeroshop.enums.ContactType;

@Entity
@Table(name = "contacts")
@Inheritance(strategy = InheritanceType.JOINED)
@Data 
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Contact {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false)
    private ContactType contactType;
    @Column(name = "observation")
    private String observation;
    
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
