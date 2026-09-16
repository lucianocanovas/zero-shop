package ingsoftware.zeroshop.entity.transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.enums.InvoiceStatus;

@Entity
@Table(name = "invoices")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;
    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;
    @Column(name = "total", nullable = false)
    private Double total;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invoice_id")
    @Builder.Default
    private List<InvoiceDetail> details = new ArrayList<>();

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

