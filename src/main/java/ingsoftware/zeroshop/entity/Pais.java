package ingsoftware.zeroshop.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "paises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)// Cascade.all sirve para que cuando se elimine la persona, se eliminen sus direcciones asociadas.
    @JoinColumn(name = "provincia_id")

    @Builder.Default
    private List<Provincia> provincias = new ArrayList<>();
}

