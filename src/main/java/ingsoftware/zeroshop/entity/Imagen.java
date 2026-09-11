package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import ingsoftware.zeroshop.enums.TipoImagen;

@Entity
@Table(name = "imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String mime;

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_imagen", nullable = false)
    private TipoImagen tipoImagen;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}
