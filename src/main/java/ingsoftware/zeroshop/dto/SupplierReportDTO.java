package ingsoftware.zeroshop.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SupplierReportDTO(
    List<SupplierComparisonDTO> comparisons
) {
    public record SupplierComparisonDTO(
        UUID productId,
        String productName,
        String productCode,
        String categoryName,
        String bestSupplierName,
        BigDecimal bestCostPrice,
        String altSupplierName,
        BigDecimal altCostPrice,
        BigDecimal unitSavings,
        Double savingPercentage
    ) {}
}
