package ingsoftware.zeroshop.entity.transaction;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.enums.OrderStatus;

@Entity
@Table(name = "orders")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Order {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

