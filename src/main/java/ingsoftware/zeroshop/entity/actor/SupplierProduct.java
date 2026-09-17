package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;
import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.entity.catalog.Product;

@Entity
@Table(name = "supplier_products", uniqueConstraints = @UniqueConstraint(columnNames = {"supplier_id", "product_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

}