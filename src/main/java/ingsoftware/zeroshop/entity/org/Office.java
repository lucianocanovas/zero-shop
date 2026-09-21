package ingsoftware.zeroshop.entity.org;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.entity.actor.Contact;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.enums.OfficeType;

@Entity
@Table(name = "offices")
@Data
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cuit", nullable = false, unique = true)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OfficeType type;

    @ManyToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organization organization;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "office_contacts",
        joinColumns = @JoinColumn(name = "office_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private java.util.Collection<Contact> contact;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "office_addresses",
        joinColumns = @JoinColumn(name = "office_id"),
        inverseJoinColumns = @JoinColumn(name = "address_id")
    )
    private java.util.Collection<Address> address;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
}