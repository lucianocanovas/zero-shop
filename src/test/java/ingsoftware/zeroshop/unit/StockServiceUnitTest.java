package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.service.org.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceUnitTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OfficeRepository officeRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    @DisplayName("Unit: Decremento de stock exitoso descuenta la cantidad correcta")
    public void testDecrementStockSuccess() {
        UUID productId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();

        Product product = Product.builder().id(productId).name("Remera Dry-Fit").build();
        Office office = new Office();
        office.setId(officeId);
        office.setName("Sucursal Mendoza");

        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .product(product)
                .office(office)
                .quantity(15)
                .build();

        when(stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        stockService.decrementStock(productId, officeId, 5);

        assertEquals(10, stock.getQuantity());
        verify(stockRepository, times(1)).save(stock);
    }

    @Test
    @DisplayName("Unit: Decremento de stock lanza IllegalStateException si las existencias son insuficientes")
    public void testDecrementStockInsufficient() {
        UUID productId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();

        Product product = Product.builder().id(productId).name("Campera Zero").build();
        Stock stock = Stock.builder().id(UUID.randomUUID()).product(product).quantity(3).build();

        when(stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId))
                .thenReturn(Optional.of(stock));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                stockService.decrementStock(productId, officeId, 10)
        );

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(stockRepository, never()).save(stock);
    }

    @Test
    @DisplayName("Unit: Incremento de stock existente suma la cantidad ingresada")
    public void testIncrementStockExisting() {
        UUID productId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();

        Stock stock = Stock.builder().id(UUID.randomUUID()).quantity(20).build();

        when(stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        stockService.incrementStock(productId, officeId, 10);

        assertEquals(30, stock.getQuantity());
        verify(stockRepository, times(1)).save(stock);
    }

    @Test
    @DisplayName("Unit: Incremento crea nuevo registro de stock si no existía previo")
    public void testIncrementStockCreatesNewIfAbsent() {
        UUID productId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();

        Product product = Product.builder().id(productId).name("Zapatillas Zero Air").build();
        Office office = new Office();
        office.setId(officeId);
        office.setName("Sucursal Mendoza");

        when(stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId))
                .thenReturn(Optional.empty());
        when(productRepository.findActive(productId)).thenReturn(Optional.of(product));
        when(officeRepository.findActive(officeId)).thenReturn(Optional.of(office));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        stockService.incrementStock(productId, officeId, 25);

        verify(stockRepository, times(1)).save(argThat(s ->
                s.getProduct().getId().equals(productId) &&
                s.getOffice().getId().equals(officeId) &&
                s.getQuantity() == 25
        ));
    }

    @Test
    @DisplayName("Unit: hasAvailableStock responde adecuadamente según existencias")
    public void testHasAvailableStock() {
        UUID productId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();

        Stock stock = Stock.builder().quantity(10).build();
        when(stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(productId, officeId))
                .thenReturn(Optional.of(stock));

        assertTrue(stockService.hasAvailableStock(productId, officeId, 5));
        assertTrue(stockService.hasAvailableStock(productId, officeId, 10));
        assertFalse(stockService.hasAvailableStock(productId, officeId, 11));
        assertFalse(stockService.hasAvailableStock(productId, officeId, 0));
        assertFalse(stockService.hasAvailableStock(productId, officeId, -1));
    }
}
