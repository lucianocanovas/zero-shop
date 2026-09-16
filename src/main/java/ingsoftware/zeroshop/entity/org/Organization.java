package ingsoftware.zeroshop.entity.org;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organization")
@Data
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}