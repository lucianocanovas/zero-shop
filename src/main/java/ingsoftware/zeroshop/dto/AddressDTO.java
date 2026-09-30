package ingsoftware.zeroshop.dto;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private String street;
    private String number;
    private String zipCode;
    private String floor;
    private String apartment;
    private String observations;
    private UUID countryId;
    private UUID stateId;
    private UUID cityId;
}
