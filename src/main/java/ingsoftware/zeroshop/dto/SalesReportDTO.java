package ingsoftware.zeroshop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SalesReportDTO(
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalBilled,
    int totalOrders,
    List<SaleItemDTO> details
) {
    public record SaleItemDTO(
        String orderId,
        LocalDateTime date,
        String productName,
        String categoryName,
        Integer quantity,
        String paymentMethod,
        BigDecimal amount
    ) {}
}
