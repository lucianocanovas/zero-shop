package ingsoftware.zeroshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierFormDTO {
    private java.util.UUID id;
    private String name;
    private String cuit;
    private String email;
    private String phone;
    @jakarta.validation.Valid
    @jakarta.validation.constraints.NotNull(message = "La dirección es obligatoria")
    private AddressDTO address = new AddressDTO();
}
