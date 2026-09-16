package ingsoftware.zeroshop.entity.org;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ingsoftware.zeroshop.entity.actor.Contact;

@Entity
@Table(name = "office_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

