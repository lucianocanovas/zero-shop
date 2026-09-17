package ingsoftware.zeroshop.entity.location;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "countries")
@Data
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column (name = "name", nullable = false)
    private String name;

    @Column (name = "code", nullable = false)
    private String code;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

}