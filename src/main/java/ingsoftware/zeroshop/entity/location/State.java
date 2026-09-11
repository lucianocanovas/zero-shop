package ingsoftware.zeroshop.entity.location;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "states")
@Data
public class State {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column (name = "name", nullable = false)
    private String name;
    @Column (name = "code", nullable = false)
    private String code;
    @OneToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;
}
