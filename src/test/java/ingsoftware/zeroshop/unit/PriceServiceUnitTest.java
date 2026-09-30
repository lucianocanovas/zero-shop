package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.catalog.PriceHistory;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.repository.catalog.PriceHistoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.service.catalog.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PriceServiceUnitTest {

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PriceService priceService;

    @Test
    @DisplayName("Unit: Actualización de precio cierra período anterior y crea nuevo registro de historial")
    public void testUpdateProductPrice() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).currentPrice(new BigDecimal("10000.00")).build();

        PriceHistory oldPrice = PriceHistory.builder()
                .id(UUID.randomUUID())
                .product(product)
                .price(new BigDecimal("10000.00"))
                .startDate(LocalDateTime.now().minusDays(70))
                .endDate(null)
                .build();

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(product));
        when(priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(productId))
                .thenReturn(Optional.of(oldPrice));
        when(priceHistoryRepository.save(any(PriceHistory.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal newPrice = new BigDecimal("12500.00");
        PriceHistory updated = priceService.updateProductPrice(productId, newPrice, true);

        assertNotNull(updated);
        assertEquals(new BigDecimal("12500.00"), updated.getPrice());
        assertNotNull(oldPrice.getEndDate(), "El precio anterior debe quedar con fecha de finalización");
        assertEquals(new BigDecimal("12500.00"), product.getCurrentPrice());
        assertTrue(product.getOnSale());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Unit: isPriceUpdateDue detecta necesidad de actualización si pasaron más de 60 días (bimestral)")
    public void testIsPriceUpdateDue() {
        UUID productId = UUID.randomUUID();

        PriceHistory expiredPrice = PriceHistory.builder()
                .startDate(LocalDateTime.now().minusDays(65))
                .build();

        when(priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(productId))
                .thenReturn(Optional.of(expiredPrice));

        assertTrue(priceService.isPriceUpdateDue(productId), "Debe marcar que requiere ajuste si pasaron 65 días");

        PriceHistory recentPrice = PriceHistory.builder()
                .startDate(LocalDateTime.now().minusDays(15))
                .build();

        when(priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(productId))
                .thenReturn(Optional.of(recentPrice));

        assertFalse(priceService.isPriceUpdateDue(productId), "No debe marcar ajuste si se actualizó hace 15 días");
    }

    @Test
    @DisplayName("Unit: applyInflationAdjustment aplica porcentaje inflacionario a los productos")
    public void testApplyInflationAdjustment() {
        UUID prodId1 = UUID.randomUUID();
        Product product1 = Product.builder().id(prodId1).currentPrice(new BigDecimal("10000.00")).build();

        when(productRepository.findAllByDeletedFalse()).thenReturn(List.of(product1));
        when(productRepository.findByIdAndDeletedFalse(prodId1)).thenReturn(Optional.of(product1));
        when(priceHistoryRepository.findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(prodId1))
                .thenReturn(Optional.of(PriceHistory.builder().price(new BigDecimal("10000.00")).startDate(LocalDateTime.now().minusDays(70)).build()));
        when(priceHistoryRepository.save(any(PriceHistory.class))).thenAnswer(i -> i.getArgument(0));

        // Ajuste del 10% por inflación
        int updated = priceService.applyInflationAdjustment(new BigDecimal("10.00"), false);

        assertEquals(1, updated);
        assertEquals(new BigDecimal("11000.00"), product1.getCurrentPrice());
    }
}
