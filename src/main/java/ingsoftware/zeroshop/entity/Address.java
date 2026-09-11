package ingsoftware.zeroshop.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direcciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String Street;

    private String number;

    private String neighborhood;

    @Column(name = "manzana_piso")
    private String blockFloor;

    @Column(name = "casa_departamento")
    private String houseApartment;

    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id") // Clave foranea. Guarda el ID de la localidad a la que pertenece la direccion.
    private City city;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;
}

