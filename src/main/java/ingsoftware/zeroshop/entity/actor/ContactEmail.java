package ingsoftware.zeroshop.entity.actor;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contactos_correos_electronicos")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactEmail extends Contact {

    @Column(nullable = false)
    private String email;
}

