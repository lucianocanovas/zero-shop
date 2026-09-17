package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "person_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

}