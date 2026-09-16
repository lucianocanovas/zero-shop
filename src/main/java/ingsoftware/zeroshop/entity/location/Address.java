package ingsoftware.zeroshop.entity.location;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "floor")
    private String floor;

    @Column(name = "apartment")
    private String apartment;

    @Column(name = "observations")
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    private City city;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
