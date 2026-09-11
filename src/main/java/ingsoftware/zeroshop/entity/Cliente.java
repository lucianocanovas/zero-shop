package ingsoftware.zeroshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Persona {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nacionalidad_id")
    private Nacionalidad nacionalidad;
}
