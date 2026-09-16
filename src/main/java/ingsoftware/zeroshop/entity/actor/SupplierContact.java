package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

