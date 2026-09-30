package ingsoftware.zeroshop.dto;

import java.util.List;

public record StockReportDTO(
    int totalStockUnits,
    double pctGood,
    double pctRegular,
    double pctBad,
    List<StockItemDTO> details
) {
    public record StockItemDTO(
        String officeName,
        String productName,
        String productCode,
        String productSize,
        int quantity,
        int capacity,
        int percentage,
        String trafficLight, // "BIEN", "REGULAR", "MALO"
        int neededUnits,
        String supplierName,
        String supplierPhone,
        String whatsappUrl
    ) {}
}
