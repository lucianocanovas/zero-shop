package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.Role;

@Entity
@Table (name = "users")
@Data
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column (name = "deleted", nullable = false)
    private Boolean deleted = false;
}
