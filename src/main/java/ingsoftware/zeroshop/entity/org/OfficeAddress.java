package ingsoftware.zeroshop.entity.org;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ingsoftware.zeroshop.entity.location.Address;

@Entity
@Table(name = "office_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

