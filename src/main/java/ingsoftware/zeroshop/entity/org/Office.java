package ingsoftware.zeroshop.entity.org;

import jakarta.persistence.*;
import lombok.*;

import ingsoftware.zeroshop.enums.OfficeType;

@Entity
@Table(name = "offices")
@Data
public class Office {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cuit", nullable = false, unique = true)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OfficeType type;

    @ManyToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organization organization;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
}