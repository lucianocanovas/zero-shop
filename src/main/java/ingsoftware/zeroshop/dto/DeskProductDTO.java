package ingsoftware.zeroshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeskProductDTO {
    private UUID id;
    private String code;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Boolean onSale;
}
