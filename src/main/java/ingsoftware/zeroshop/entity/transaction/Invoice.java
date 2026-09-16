package ingsoftware.zeroshop.entity.transaction;

import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.InvoiceStatus;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "number", nullable = false, unique = true)
    private String number;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}