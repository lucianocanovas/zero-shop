package ingsoftware.zeroshop.load;

import ingsoftware.zeroshop.dto.StockReportDTO;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.service.catalog.ProductService;
import ingsoftware.zeroshop.service.report.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de Carga y Rendimiento (Throughput & Latency):
 * Somete a la capa de servicios a ráfagas concurrentes de consultas intensivas de catálogo,
 * filtros y cálculo de reportes analíticos con DTOs para medir throughput y tiempos de respuesta.
 */
@SpringBootTest
public class SystemThroughputLoadTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReportService reportService;

    @Test
    @DisplayName("Carga/Rendimiento: 50 solicitudes concurrentes a catálogo y reportes mantienen alta disponibilidad y baja latencia")
    public void testThroughputUnderConcurrentLoad() throws InterruptedException {
        final int REQUEST_COUNT = 50;
        final int POOL_SIZE = 10;

        ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(REQUEST_COUNT);

        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger errorCounter = new AtomicInteger(0);

        long globalStartTime = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    startSignal.await(); // Disparo sincronizado
                    long reqStart = System.nanoTime();

                    if (taskId % 2 == 0) {
                        // Carga sobre Catálogo y Búsqueda
                        List<Product> products = productService.searchProducts("zapatillas", null, null, null);
                        assertNotNull(products);
                    } else {
                        // Carga sobre Módulo Analítico de Reportes
                        StockReportDTO stockReport = reportService.getStockReport();
                        assertNotNull(stockReport);
                    }

                    long reqEnd = System.nanoTime();
                    long durationMs = (reqEnd - reqStart) / 1_000_000;
                    latencies.add(durationMs);
                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    errorCounter.incrementAndGet();
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        // Liberar todos los hilos
        startSignal.countDown();
        boolean finished = doneSignal.await(20, TimeUnit.SECONDS);
        executor.shutdown();

        long globalEndTime = System.currentTimeMillis();
        long totalDurationMs = globalEndTime - globalStartTime;

        assertTrue(finished, "La batería de pruebas de carga debió concluir antes del timeout");
        assertEquals(REQUEST_COUNT, successCounter.get(), "Todas las solicitudes debieron responder con éxito");
        assertEquals(0, errorCounter.get(), "No deben presentarse excepciones ni caídas bajo carga");

        // Estadísticas de rendimiento
        double avgLatency = latencies.stream().mapToLong(l -> l.longValue()).average().orElse(0.0);
        long maxLatency = latencies.stream().mapToLong(l -> l.longValue()).max().orElse(0L);
        long minLatency = latencies.stream().mapToLong(l -> l.longValue()).min().orElse(0L);
        double throughputRps = (REQUEST_COUNT * 1000.0) / totalDurationMs;

        System.out.printf("""
            ========== MÉTRICAS DE RENDIMIENTO BAJO CARGA ==========
            Solicitudes enviadas : %d
            Solicitudes exitosas : %d
            Errores detectados   : %d
            Tiempo total corrida : %d ms
            Throughput estimado  : %.2f ops/segundo
            Latencia promedio    : %.2f ms
            Latencia mínima      : %d ms
            Latencia máxima      : %d ms
            ========================================================
            """, REQUEST_COUNT, successCounter.get(), errorCounter.get(),
                totalDurationMs, throughputRps, avgLatency, minLatency, maxLatency);

        // Aserciones de calidad: latencia promedio menor a 500ms en ambiente local
        assertTrue(avgLatency < 500, "La latencia promedio debe ser inferior a 500 ms");
    }
}
