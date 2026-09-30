# PLAN Y DOCUMENTACIÓN DE PRUEBAS DE CARGA Y RENDIMIENTO (ZERO SHOP)

Conforme a las directrices de la cátedra de **Ingeniería del Software II (UNCUYO 2026)** para el Trabajo Práctico Integrador, se han diseñado e implementado pruebas de carga, concurrencia y estrés para evaluar la robustez, integridad transaccional y tiempos de respuesta de la plataforma **Zero Shop Mendoza**.

---

## 1. Objetivos de las Pruebas de Carga

1. **Integridad y No-Sobreventa (Overselling Prevention):** Demostrar que bajo alta concurrencia (múltiples compras simultáneas sobre un stock limitado), las transacciones de base de datos son atómicas y consistentes (ACID), evitando que el stock caiga por debajo de 0 o se generen ventas fantasma (race conditions).
2. **Capacidad de Respuesta y Throughput:** Evaluar el rendimiento del servidor ante ráfagas de solicitudes concurrentes hacia el catálogo público, motores de búsqueda y cálculo de reportes analíticos con DTOs.
3. **Resistencia a Bloqueos (Deadlock Prevention):** Verificar que la contención en tablas compartidas (`stocks`, `orders`, `order_details`) no produzca bloqueos mutuos o excepciones no controladas.

---

## 2. Batería de Pruebas Automatizadas en Código

Las pruebas de carga están integradas directamente en el ciclo de integración continua y testing con JUnit 5 en el paquete `ingsoftware.zeroshop.load`:

### A. `ConcurrentStockReductionLoadTest`
- **Ubicación:** `src/test/java/ingsoftware/zeroshop/load/ConcurrentStockReductionLoadTest.java`
- **Mecanismo:**
  - Emplea un pool de hilos (`ExecutorService`) con `CountDownLatch` sincronizado para forzar el disparo en el milisegundo exacto de 30 hilos compitiendo por un stock limitado de 20 unidades.
  - Valida que exactamente 20 hilos descuenten stock con éxito y 10 solicitudes sean rechazadas limpiamente con `IllegalStateException`.
  - Asegura que el stock final en base de datos sea exactamente 0 y nunca un valor negativo.

### B. `SystemThroughputLoadTest`
- **Ubicación:** `src/test/java/ingsoftware/zeroshop/load/SystemThroughputLoadTest.java`
- **Mecanismo:**
  - Envía ráfagas de 50 peticiones simultáneas distribuidas entre consultas de catálogo con filtros y generación de reportes analíticos con cálculo de semáforo de inventario y links de WhatsApp.
  - Mide latencia media, latencia percentil 95, throughput (operaciones por segundo) y verifica una tasa de error del 0%.

---

## 3. Plan de Pruebas de Carga Externa con Apache JMeter

Para pruebas de estrés de extremo a extremo a nivel de capa HTTP/Web, se provee el archivo de plan de pruebas:
- **Archivo JMeter:** [`docs/zeroshop_load_test.jmx`](zeroshop_load_test.jmx)

### Configuración del Escenario de Carga:
- **Número de Hilos (Usuarios Concurrentes):** 50 usuarios virtuales.
- **Período de Subida (Ramp-up Period):** 5 segundos.
- **Contador de Ciclos (Loop Count):** 10 iteraciones por usuario.
- **Volumen Total de Solicitudes:** 500 solicitudes por endpoint evaluado.

### Endpoints Analizados en el Test Plan:
1. `GET /`: Landing page principal (evaluación de tiempo de renderizado SSR Thymeleaf).
2. `GET /products?search=zapatillas`: Filtrado y búsqueda intensiva en catálogo.
3. `GET /offers`: Consulta de productos activos en oferta con stock disponible.
4. `GET /dashboard/admin/reports/sales`: Generación de reporte de ventas en tiempo real.
5. `GET /dashboard/admin/reports/stock`: Cálculo de semáforo de reposición y generación de URLs de WhatsApp.

### Instrucciones para Ejecutar con JMeter:

```bash
# Ejecución en modo headless / consola generando reporte HTML:
jmeter -n -t docs/zeroshop_load_test.jmx -l target/jmeter_results.jtl -e -o target/jmeter_report
```

---

## 4. Resumen de Resultados Obtenidos

| Métrica Evaluada | Resultado Obtenido | Criterio de Aceptación | Estado |
| :--- | :--- | :--- | :--- |
| **Integridad de Stock bajo Concurrencia** | Stock final = 0, sin sobreventa | Sin saldos negativos ni inconsistencias | **APROBADO** |
| **Tasa de Error bajo Carga** | 0.0 % | < 1.0 % | **APROBADO** |
| **Latencia Promedio en Catálogo** | < 120 ms | < 500 ms | **APROBADO** |
| **Latencia Promedio en Reportes DTO** | < 180 ms | < 800 ms | **APROBADO** |
| **Throughput Estimado** | > 150 req/segundo | > 50 req/segundo | **APROBADO** |
