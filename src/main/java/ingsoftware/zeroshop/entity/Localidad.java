package ingsoftware.zeroshop.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "localidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Localidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    @ManyToOne(fetch = FetchType.LAZY)  
    @JoinColumn(name = "departamento_id") // Se junta con ID, Cada localidad tiene asignada directamente un departamento.
    private Departamento departamento;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}

