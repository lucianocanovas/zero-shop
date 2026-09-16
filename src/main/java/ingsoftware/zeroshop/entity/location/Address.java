package ingsoftware.zeroshop.entity.location;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

import ingsoftware.zeroshop.entity.actor.Person;

@Entity
@Table(name = "addresses")
@Data
public class Address {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column (name = "street", nullable = false)
    private String street;
    @Column (name = "number", nullable = false)
    private String number;
    @Column (name= "floor")
    private String floor;
    @Column (name= "apartment")
    private String apartment;
    @Column (name= "observations")
    private String observations;
    
    @ManyToOne
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    private City city;
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;
    
    @Column (name = "deleted", nullable = false)
    private Boolean deleted;
}
