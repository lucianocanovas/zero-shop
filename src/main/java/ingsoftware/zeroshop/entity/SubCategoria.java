package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "subcategorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}
