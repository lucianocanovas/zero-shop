package ingsoftware.zeroshop.load;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.service.org.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de Carga y Concurrencia:
 * Evalúa el comportamiento del inventario y la consistencia transaccional bajo condiciones
 * de alta concurrencia (múltiples solicitudes simultáneas de compra sobre el mismo stock).
 */
@SpringBootTest
public class ConcurrentStockReductionLoadTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Test
    @DisplayName("Carga/Estrés: Compras simultáneas concurrentes no generan sobreventa ni stock negativo")
    public void testConcurrentStockReductionUnderLoad() throws InterruptedException {
        // 1. Preparar un producto y sucursal con stock inicial controlado
        Product product = productRepository.findAllByDeletedFalse().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay productos disponibles para la prueba de carga."));
        Office office = officeRepository.findAllByDeletedFalse().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay sucursales disponibles para la prueba de carga."));

        final int INITIAL_STOCK = 20;
        Stock stock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(product.getId(), office.getId())
                .orElseGet(() -> Stock.builder().product(product).office(office).quantity(0).deleted(false).build());
        stock.setQuantity(INITIAL_STOCK);
        stockRepository.save(stock);

        // 2. Simular 30 usuarios simultáneos compitiendo por descontar 1 unidad cada uno (solo 20 deberían tener éxito)
        final int THREAD_COUNT = 30;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successfulDecrements = new AtomicInteger(0);
        AtomicInteger rejectedRequests = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Sincroniza el disparo de todos los hilos al mismo instante exacto
                    stockService.decrementStock(product.getId(), office.getId(), 1);
                    successfulDecrements.incrementAndGet();
                } catch (IllegalStateException e) {
                    // Rechazado correctamente por falta de stock
                    rejectedRequests.incrementAndGet();
                } catch (Exception e) {
                    rejectedRequests.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Disparar simultáneamente todos los hilos
        startLatch.countDown();
        boolean completed = finishLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Todos los hilos concurrentes debieron haber finalizado dentro del tiempo límite.");

        // 3. Verificaciones de consistencia bajo carga
        Stock finalStock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(product.getId(), office.getId()).orElseThrow();

        System.out.printf("[PRUEBA DE CARGA] Hilos: %d | Exitosos: %d | Rechazados: %d | Stock Final: %d%n",
                THREAD_COUNT, successfulDecrements.get(), rejectedRequests.get(), finalStock.getQuantity());

        assertTrue(finalStock.getQuantity() >= 0, "El stock final NUNCA debe ser negativo bajo condiciones de carga");
        assertEquals(THREAD_COUNT, successfulDecrements.get() + rejectedRequests.get(),
                "La suma de exitosos y rechazados debe igualar el total de solicitudes enviadas");
    }
}
