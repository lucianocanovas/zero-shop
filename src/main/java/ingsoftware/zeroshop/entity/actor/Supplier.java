package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table (name = "suppliers")
@Data
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column (name = "deleted", nullable = false)
    private Boolean deleted = false;
    
}