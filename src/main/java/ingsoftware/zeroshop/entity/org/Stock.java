package ingsoftware.zeroshop.entity.org;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.entity.catalog.Product;

@Entity
@Table(name = "stock", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "office_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", referencedColumnName = "id", nullable = false)
    private Office office;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
