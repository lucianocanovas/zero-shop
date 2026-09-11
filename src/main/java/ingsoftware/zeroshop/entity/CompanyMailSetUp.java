package ingsoftware.zeroshop.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuraciones_correo_empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyMailSetUp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String mail;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String port;

    @Column(nullable = false)
    private String smtp;

    @Column(nullable = false)
    private boolean tls;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;
}

