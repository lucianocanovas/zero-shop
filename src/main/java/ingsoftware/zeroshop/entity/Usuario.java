package ingsoftware.zeroshop.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.Role;

@Entity
@Table(name = "users");
@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID) 
    private UUID id;
    @Column(nullable = false) 
    private String nombreUsuario;
    @Column(nullable = false, unique = true) 
    private String email;
    @Column(nullable = false) 
    private String password;
    @Enumerated(EnumType.STRING) 
    private Role role;

    @Column(nullable = false)
    private boolean eliminado;
}