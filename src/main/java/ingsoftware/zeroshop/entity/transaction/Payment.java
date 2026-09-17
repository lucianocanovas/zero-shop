package ingsoftware.zeroshop.entity.transaction;

import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.PaymentMethod;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
}