package ingsoftware.zeroshop.service.actor;

import ingsoftware.zeroshop.entity.actor.User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    // Lógica de negocio para gestión de usuarios, autenticación y perfiles

    public List<User> findAll() {
        return Collections.emptyList();
    }

    public Optional<User> findById(UUID id) {
        return Optional.empty();
    }

    public User getByEmail(String email) {
        return null;
    }

    public String getUserFirstName(String email) {
        return "";
    }

    public User updateProfile(UUID id, String firstName, String lastName, String email, String password) {
        return null;
    }

    public void updateNonAdminProfile(UUID id, String firstName, String lastName, String email, String password) {
    }

    public void deleteNonAdmin(UUID id) {
    }

}

