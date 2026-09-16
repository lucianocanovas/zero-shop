package ingsoftware.zeroshop.entity.transaction;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier_invoices")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupplierInvoice extends Invoice {}
