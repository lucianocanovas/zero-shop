package ingsoftware.zeroshop.entity.location;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ingsoftware.zeroshop.entity.actor.Supplier;

@Entity
@Table(name = "supplier_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
}