package ingsoftware.zeroshop.entity.transaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.org.Office;

@Entity
@Table(name = "sale_orders")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SaleOrder extends Order {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;
}

