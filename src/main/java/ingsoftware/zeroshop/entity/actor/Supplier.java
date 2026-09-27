package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.entity.location.Address;

@Entity
@Table(name = "suppliers")
@Data
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false) // Razon social
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "supplier_contacts", joinColumns = @JoinColumn(name = "supplier_id"), inverseJoinColumns = @JoinColumn(name = "contact_id"))
    private java.util.Collection<Contact> contact;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "supplier_addresses", joinColumns = @JoinColumn(name = "supplier_id"), inverseJoinColumns = @JoinColumn(name = "address_id"))
    private java.util.Collection<Address> address;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "cuit")
    private String cuit;

}