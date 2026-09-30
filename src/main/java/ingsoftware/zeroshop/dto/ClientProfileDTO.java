package ingsoftware.zeroshop.dto;

import ingsoftware.zeroshop.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfileDTO {

    // 1. Datos Personales
    private String firstName;
    private String lastName;
    private Gender gender;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    private String phone;

    // 2. Dirección de Entrega en Mendoza
    private String state;
    private String department;
    private String city;
    private String street;
    private String number;
    private String zipCode;
    private String floor;
    private String apartment;

    // 3. Cuenta y Seguridad
    private String email;
    private String password;
}
