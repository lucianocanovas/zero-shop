package ingsoftware.zeroshop.entity.org;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company")
@Data
public class Company {
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}