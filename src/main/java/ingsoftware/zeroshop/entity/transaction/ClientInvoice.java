package ingsoftware.zeroshop.entity.transaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.entity.actor.Client;

@Entity
@Table(name = "client_invoices")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClientInvoice extends Invoice {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id")
    private SaleOrder saleOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
}

