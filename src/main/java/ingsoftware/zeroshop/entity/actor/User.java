package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import ingsoftware.zeroshop.enums.Role;

@Entity
@Table(name = "user")
@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID) 
    private UUID id;
    @Column(nullable = false, unique = true) 
    private String email;
    @Column(nullable = false) 
    private String password;
    @Enumerated(EnumType.STRING) 
    private Role role;
    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;
    // Relación con Persona (pertenece a una Persona)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Person person;
}