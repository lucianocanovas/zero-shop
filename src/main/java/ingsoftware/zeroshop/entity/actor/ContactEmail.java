package ingsoftware.zeroshop.entity.actor;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contact_emails")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactEmail extends Contact {
    @Column(name = "email", nullable = false)
    private String email;
}