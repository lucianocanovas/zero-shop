package ingsoftware.zeroshop.repository;

import ingsoftware.zeroshop.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonaRepository extends JpaRepository<Persona, UUID> {
}
