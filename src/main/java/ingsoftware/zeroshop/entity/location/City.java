package ingsoftware.zeroshop.entity.location;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cities")
@Data
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column (name = "name", nullable = false)
    private String name;

    @Column (name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "state_id", referencedColumnName = "id")
    private State state;

    @Column (name = "deleted", nullable = false)
    private Boolean deleted = false;
    
}
