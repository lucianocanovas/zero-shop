package ingsoftware.zeroshop.entity.catalog;

import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "price_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "price", nullable = false)
    private Double price;
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}