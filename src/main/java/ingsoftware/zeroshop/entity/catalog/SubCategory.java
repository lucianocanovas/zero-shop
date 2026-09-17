package ingsoftware.zeroshop.entity.catalog;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subcategories", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "category_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
}
