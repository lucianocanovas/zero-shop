package ingsoftware.zeroshop.entity.transaction;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_invoices")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClientInvoice extends Invoice {}