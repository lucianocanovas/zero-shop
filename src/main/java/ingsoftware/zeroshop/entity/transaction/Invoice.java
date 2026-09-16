package ingsoftware.zeroshop.entity.transaction;

import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.InvoiceStatus;

@Entity
@Table(name = "invoices")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "number", nullable = false, unique = true)
    private String number;
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}