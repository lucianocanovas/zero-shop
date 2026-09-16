package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;

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
    @Id 
    @Column(name = "client_id", nullable = false)
    private UUID clientId;
}