package ingsoftware.zeroshop.entity.actor;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table (name = "clients")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Client extends Person {
    @Column(name = "client_number", nullable = false)
    private String clientNumber;
}