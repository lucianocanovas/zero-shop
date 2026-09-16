package ingsoftware.zeroshop.entity.actor;

import java.util.UUID;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import ingsoftware.zeroshop.enums.Gender;
import ingsoftware.zeroshop.enums.IDType;

@Entity 
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
@Data 
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private IDType idType;
    @Column(name = "id_number", nullable = false, unique = true)
    private String idNumber;
    
    @Column (name = "deleted", nullable = false)
    private Boolean deleted = false;}
