package ingsoftware.zeroshop.entity.transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    @Column(name = "total", nullable = false)
    private Double total;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderDetail> details = new ArrayList<>();

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

