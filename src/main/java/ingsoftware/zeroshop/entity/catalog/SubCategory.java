package ingsoftware.zeroshop.entity.catalog;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subcategories")
@Data
public class SubCategory {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @Column (name = "deleted", nullable = false)
    private Boolean deleted = false;
}
