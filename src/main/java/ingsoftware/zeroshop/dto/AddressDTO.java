package ingsoftware.zeroshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    @NotBlank(message = "La calle es obligatoria")
    private String street;

    @NotBlank(message = "El número es obligatorio")
    private String number;

    @NotBlank(message = "El código postal es obligatorio")
    private String zipCode;

    private String floor;

    private String apartment;

    private String observations;

    @NotNull(message = "El país es obligatorio")
    private UUID countryId;

    @NotNull(message = "La provincia/estado es obligatoria")
    private UUID stateId;

    @NotNull(message = "La ciudad es obligatoria")
    private UUID cityId;
}
