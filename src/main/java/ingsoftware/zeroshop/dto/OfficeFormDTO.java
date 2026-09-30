package ingsoftware.zeroshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class OfficeFormDTO {
    private UUID id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El CUIT es obligatorio")
    private String cuit;

    private String phone;

    @NotBlank(message = "El tipo de establecimiento es obligatorio")
    private String type;

    private AddressDTO address = new AddressDTO();
}
