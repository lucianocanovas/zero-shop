# ESTRATEGIA Y SUITE DE PRUEBAS EN ZERO SHOP

El sistema informático E-Commerce **Zero Shop** cuenta con una batería completa de pruebas automatizadas diseñadas para garantizar la fiabilidad, robustez, seguridad e integridad transaccional de la aplicación, dando cumplimiento estricto a las exigencias de calidad del **Trabajo Práctico Integrador N°1 (Ingeniería del Software II - UNCUYO 2026)**.

---

## 1. Pirámide y Arquitectura de Pruebas

La suite de pruebas sigue el modelo clásico de la **Pirámide de Testing**, complementado con pruebas de **Concurrencia y Carga**:

```text
                  ▲
                 / \
                /   \     Pruebas de Carga y Concurrencia (Load & Concurrency)
               /  C  \    - Estrés transaccional y prevención de sobreventa
              /───────\   - Throughput (ops/s), latencia y plan Apache JMeter
             /         \
            /     I     \   Pruebas de Integración (Integration)
           /─────────────\  - Persistencia JPA real (PostgreSQL)
          /               \ - Filtros de Spring Security y MockMvc
         /        U        \
        /───────────────────\ Pruebas Unitarias Aisladas (Unit)
       /                     \ - Lógica de negocio pura con Mockito
      └───────────────────────┘ - Sin dependencias externas ni I/O pesado
```

### Tecnologías y Librerías Utilizadas

- **JUnit 5 (Jupiter):** Framework estándar para la definición del ciclo de vida de los tests (`@Test`, `@BeforeEach`, `@DisplayName`).
- **Mockito:** Aislamiento de dependencias, dobles de prueba (`@Mock`, `@InjectMocks`), simulación de comportamientos (`when().thenReturn()`) y verificación de invocaciones (`verify()`).
- **Spring Boot Test:** Contexto de integración de la aplicación con inyección de beans reales (`@SpringBootTest`).
- **Spring Security Test:** Verificación de autenticación y autorización mediante identidades sintéticas (`@WithMockUser`, `@WithAnonymousUser`).
- **MockMvc:** Simulación de peticiones HTTP en el despachador de Spring sin levantar un servidor de sockets web real.
- **Java Concurrency API:** Uso de `ExecutorService`, `ThreadPoolExecutor` y `CountDownLatch` para someter la lógica de negocio a concurrencia real y condiciones de carrera (*race conditions*).
- **Apache JMeter:** Plan de pruebas de estrés y rendimiento HTTP masivo (`docs/zeroshop_load_test.jmx`).

---

## 2. Organización del Código de Pruebas

Los tests se encuentran organizados dentro de `src/test/java/ingsoftware/zeroshop/` bajo una estructura de paquetes limpia y tipificada:

| Paquete | Tipo de Prueba | Enfoque Principal |
| :--- | :--- | :--- |
| `ingsoftware.zeroshop.unit` | Pruebas Unitarias | Servicios de negocio aislados con Mockito sin tocar la base de datos. |
| `ingsoftware.zeroshop.integration` | Pruebas de Integración | Verificación de seguridad web (MockMvc) y consultas analíticas a base de datos. |
| `ingsoftware.zeroshop.load` | Carga y Concurrencia | Concurrencia multihilo, consistencia transaccional y métricas de throughput. |
| `ingsoftware.zeroshop` (raíz) | Integración de Flujos | Integración de flujos de compra, ciclo de vida de usuario y SDK Mercado Pago. |

---

## 3. Catálogo Detallado de Pruebas

### 3.1. Pruebas Unitarias (`ingsoftware.zeroshop.unit`)

Total: **33 tests** | Tiempo de ejecución: **~1.5 segundos** | Estado: **100% PASS**

#### `UserServiceUnitTest` (6 tests)
Valida la lógica de registro, autenticación, activación y unicidad de usuarios:
1. `testRegisterClientSuccess`: Registro exitoso de cliente con encriptación BCrypt de la contraseña y generación de código de verificación de 6 dígitos.
2. `testRegisterClientDuplicateUsername`: Rechazo y lanzamiento de `IllegalArgumentException` al intentar registrar un email/username ya existente.
3. `testRegisterClientDuplicateDni`: Rechazo al intentar registrar una persona con número de documento duplicado.
4. `testVerifyAccountSuccess`: Validación exitosa del código de 6 dígitos, marcando la cuenta como verificada (`verified = true`) y limpiando el código de activación.
5. `testVerifyAccountInvalidCode`: Rechazo con excepción ante un código de verificación erróneo o inexistente.
6. `testResendVerificationCode`: Generación de un nuevo código numérico de 6 dígitos y reenvío mediante `EmailService`.

#### `ProductServiceUnitTest` (3 tests)
Valida la administración del catálogo de productos y precios:
1. `testCreateProductSuccess`: Creación de producto con código SKU único, validación de talle y asociación del precio base en el historial `PriceHistory`.
2. `testFindOnSaleProducts`: Recuperación del listado de productos en oferta (`onSale = true`) enriquecidos con su precio vigente y stock global disponible.
3. `testSearchProductsFilter`: Búsqueda facetada de catálogo filtrando concurrentemente por término de texto, subcategoría y tope de precio máximo (`maxPrice`).

#### `StockServiceUnitTest` (5 tests)
Valida el control de inventario y semáforo de disponibilidad:
1. `testAddStockNegativeQuantity`: Rechazo ante cantidades nulas o negativas al intentar ingresar mercadería.
2. `testAddStockSuccess`: Incremento exitoso de unidades en una sucursal/depósito existente.
3. `testReduceStockSuccess`: Descuento exitoso de existencias cuando la disponibilidad en depósito es suficiente.
4. `testReduceStockInsufficientException`: Lanzamiento de `IllegalStateException` y bloqueo de la operación ante stock insuficiente.
5. `testGetStockTrafficLight`: Clasificación correcta del nivel de stock según los umbrales del integrador:
   - `0 unidades` → Semáforo **ROJO** (Crítico).
   - `5 unidades` → Semáforo **AMARILLO** (Bajo).
   - `25 unidades` → Semáforo **VERDE** (Óptimo).

#### `PriceServiceUnitTest` (3 tests)
Valida la actualización de precios y el ajuste bimensual por inflación:
1. `testUpdateProductPriceClosesPreviousPrice`: Registro de nuevo precio histórico cerrando la vigencia (`endDate = now`) del precio anterior para preservar la trazabilidad.
2. `testApplyInflationAdjustmentBimonthly`: Ajuste masivo bimensual por inflación (ej. +15% generalizado a todos los productos activos) recalculando precios y generando nuevos registros históricos.
3. `testApplyInflationAdjustmentNegativePercentage`: Validación contra porcentajes de inflación inválidos o negativos.

#### `NewsletterServiceUnitTest` (3 tests)
Valida el despacho periódico de novedades y promociones:
1. `testSendBiweeklyOffersNewsletterWithOffers`: Envío exitoso de correo a todos los clientes registrados cuando existen productos con bandera `onSale = true`.
2. `testSendBiweeklyOffersNewsletterNoOffers`: Omisión controlada del despacho cuando no existen ofertas activas en el catálogo.
3. `testBuildNewsletterHtmlContent`: Generación del cuerpo HTML responsivo con imágenes, precios promocionales y botón de acceso a la tienda.

#### `ReportServiceUnitTest` (3 tests)
Valida la generación de los reportes gerenciales obligatorios en formato DTO (Records):
1. `testGenerateSalesReport`: Cálculo de totales facturados, recuento de órdenes y detalle discriminado por fecha, cliente y método de pago en un período específico.
2. `testGenerateStockTrafficLightReport`: Consolidación del inventario por producto con indicación del color de semáforo y enlace preformateado a la API de WhatsApp del proveedor para reposición inmediata.
3. `testGenerateCheapestSupplierReport`: Determinación precisa del proveedor que ofrece el menor costo unitario para cada producto del catálogo.

#### `SaleOrderServiceUnitTest` (4 tests)
Valida el ciclo de vida del carrito y la orden de venta:
1. `testAddToCartCreatesOrderWhenNotExists`: Creación automática de orden en estado `ON_CART` al agregar el primer producto al carrito.
2. `testCalculateOrderTotal`: Suma algebraica correcta de subtotales por ítem (precio × cantidad) reflejada en el monto total de la orden.
3. `testCompleteOrderDeductsStock`: Transición de la orden a estado `COMPLETED` con descuento atómico del inventario correspondiente.
4. `testCancelOrderRestoresStock`: Cancelación de una orden confirmada con reintegro automático de las unidades al stock del depósito.

#### `PurchaseOrderServiceUnitTest` (3 tests)
Valida el circuito de reabastecimiento con proveedores:
1. `testCreatePurchaseOrder`: Creación de orden de compra a proveedor en estado `PENDING` con detalle de ítems y costo pactado.
2. `testReceivePurchaseOrderIncrementsStock`: Recepción formal de mercadería (`RECEIVED`) con incremento automático de existencias en el depósito correspondiente.
3. `testReceiveOrderAlreadyReceivedThrowsException`: Protección contra recepciones duplicadas impidiendo acreditar dos veces la misma orden.

#### `MercadoPagoSdkUnitTest` (3 tests)
Valida la integración con la pasarela de pagos:
1. `testSdkInitialization`: Inicialización del SDK con el Access Token de prueba configurado.
2. `testCreatePreference`: Generación de la preferencia de pago de Mercado Pago con URLs de retorno (*back_urls*).
3. `testPreferenceErrorHandling`: Manejo controlado de excepciones ante parámetros inválidos.

---

### 3.2. Pruebas de Integración (`ingsoftware.zeroshop.integration` y raíz)

Total: **35 tests** | Tiempo de ejecución: **~18 segundos** | Estado: **100% PASS**

#### `SecurityAccessIntegrationTest` (5 tests)
Valida las reglas del filtro de seguridad [SecurityConfig.java](file:///c:/Users/Lucho/Desktop/zero-shop/src/main/java/ingsoftware/zeroshop/config/SecurityConfig.java):
1. `testPublicEndpointsAccessibleAnonymously`: Acceso público libre a `/`, `/products`, `/offers`, `/login`, `/register` y `/verify` sin requerir credenciales.
2. `testProtectedEndpointsRedirectToLogin`: Redirección automática al formulario `/login` cuando un usuario no autenticado intenta acceder a `/profile`, `/orders` o `/dashboard`.
3. `testClientDeniedFromDashboard`: Bloqueo con código HTTP `403 Forbidden` cuando un usuario con rol `ROLE_CLIENT` intenta ingresar a rutas administrativas (`/dashboard`).
4. `testEmployeeCanAccessDeskAndProducts`: Autorización correcta para usuarios con rol `ROLE_EMPLOYEE` sobre las vistas de mostrador (`/dashboard/desk`) y listado de productos.
5. `testAdminCanAccessAllDashboard`: Autorización irrestricta para usuarios con rol `ROLE_ADMIN` sobre reportes, administración de sucursales y gestión global.

#### `ReportIntegrationTest` (3 tests)
Valida la ejecución real de consultas JPQL/SQL en base de datos PostgreSQL:
1. `testSalesReportExecutionWithDatabase`: Agregación de ventas completadas y cálculo de facturación real.
2. `testStockReportExecutionWithDatabase`: Cálculo de disponibilidad acumulada de todas las sucursales y etiquetado de semáforo.
3. `testCheapestSupplierReportExecutionWithDatabase`: Resolución de relaciones `SupplierProduct` ordenadas por costo unitario mínimo.

#### `UserServiceIntegrationTest` (10 tests)
Valida el ciclo de vida completo de clientes y empleados persistidos:
- Creación, modificación y borrado lógico de personas y usuarios.
- Validación de integridad referencial con direcciones y contactos.
- Autenticación real contra `CustomUserDetailsService` de Spring Security.

#### `CheckoutAndPaymentFlowIntegrationTest` (7 tests)
Valida el flujo integral de compra en línea:
- Transición de carrito `ON_CART` a checkout.
- Selección de método de pago (Mercado Pago / Efectivo).
- Emisión de factura e ítems de factura asociados.

#### `StockAndCashPaymentIntegrationTest` (2 tests)
Valida la venta presencial por mostrador:
- Cobro en efectivo por empleado.
- Impacto inmediato en el stock de la sucursal del empleado.

#### `ProductCrudIntegrationTest` (5 tests)
Valida el mantenimiento de catálogo:
- Altas, bajas lógicas y modificaciones de productos y categorías.
- Registro secuencial de precios en `PriceHistory`.

#### `PurchaseFlowIntegrationTest` y `PurchaseOrderIntegrationTest` (3 tests)
Valida el circuito de compra a proveedores:
- Emisión de orden de compra a proveedor externo.
- Registro de comprobante y acreditación de inventario en depósito.

---

### 3.3. Pruebas de Carga, Estrés y Concurrencia (`ingsoftware.zeroshop.load`)

Total: **2 suites Java** + **1 plan Apache JMeter** | Estado: **100% PASS**

#### `ConcurrentStockReductionLoadTest`
- **Escenario:** Simulación de una venta masiva concurrente (*Flash Sale*). Se configuran **20 unidades de stock disponibles** en depósito y se lanzan **30 hilos de ejecución simultáneos**, cada uno intentando descontar 1 unidad mediante `stockService.reduceStock()`.
- **Sincronización:** Se utiliza un `CountDownLatch` de disparo para garantizar que todos los hilos arranquen exactamente en el mismo milisegundo.
- **Resultados Verificados:**
  - ✅ Operaciones exitosas: exactamente **20**.
  - ✅ Operaciones rechazadas por falta de stock: exactamente **10**.
  - ✅ Stock final en depósito: exactamente **0 unidades**.
  - ✅ Integridad transaccional: **Cero sobreventa** (*zero overselling*), demostrando la efectividad del aislamiento transaccional `@Transactional`.

#### `SystemThroughputLoadTest`
- **Escenario:** Inyección de **50 solicitudes concurrentes** continuas sobre las operaciones más complejas del sistema (búsqueda facetada de catálogo con cálculo de stock y generación del reporte de semáforo).
- **Métricas Registradas:**
  - Solicitudes procesadas: **50 / 50** (100% de éxito).
  - Tasa de errores: **0.0%**.
  - Tiempo total de corrida: **273 ms**.
  - Throughput estimado: **183.15 operaciones / segundo**.
  - Latencia promedio: **50.76 ms** (Mínima: 15 ms, Máxima: 137 ms).

#### Plan de Pruebas de Carga Apache JMeter (`docs/zeroshop_load_test.jmx`)
Diseñado para pruebas de rendimiento a nivel de protocolo HTTP:
- **Grupo de Hilos:** 50 usuarios virtuales concurrentes (*threads*).
- **Rampa de subida (*ramp-up*):** 10 segundos.
- **Iteraciones (*loop count*):** 10 repeticiones por usuario (500 peticiones en total).
- **Rutas Monitoreadas:**
  1. `GET /` (Página principal)
  2. `GET /products` (Catálogo con filtros)
  3. `GET /offers` (Ofertas especiales)
  4. `GET /dashboard/admin/reports/stock` (Reporte semáforo)
- Para instrucciones de uso detalladas, consultar [LOAD_TESTING.md](LOAD_TESTING.md).

---

## 4. Matriz Resumen de la Suite de Pruebas

| Suite de Pruebas | Clase de Test | Tests | Fallos | Errores | Tiempo | Estado |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **Unit - Actor** | `UserServiceUnitTest` | 6 | 0 | 0 | 0.13 s | ✅ PASS |
| **Unit - Catalog** | `ProductServiceUnitTest` | 3 | 0 | 0 | 0.24 s | ✅ PASS |
| **Unit - Catalog** | `PriceServiceUnitTest` | 3 | 0 | 0 | 0.16 s | ✅ PASS |
| **Unit - Org** | `StockServiceUnitTest` | 5 | 0 | 0 | 0.03 s | ✅ PASS |
| **Unit - Notification** | `NewsletterServiceUnitTest` | 3 | 0 | 0 | 0.42 s | ✅ PASS |
| **Unit - Report** | `ReportServiceUnitTest` | 3 | 0 | 0 | 0.20 s | ✅ PASS |
| **Unit - Transaction** | `SaleOrderServiceUnitTest` | 4 | 0 | 0 | 0.26 s | ✅ PASS |
| **Unit - Transaction** | `PurchaseOrderServiceUnitTest` | 3 | 0 | 0 | 0.34 s | ✅ PASS |
| **Unit - Payment** | `MercadoPagoSdkUnitTest` | 3 | 0 | 0 | 1.12 s | ✅ PASS |
| **Integration - Report** | `ReportIntegrationTest` | 3 | 0 | 0 | 0.85 s | ✅ PASS |
| **Integration - Security** | `SecurityAccessIntegrationTest` | 5 | 0 | 0 | 0.93 s | ✅ PASS |
| **Integration - User** | `UserServiceIntegrationTest` | 10 | 0 | 0 | 1.12 s | ✅ PASS |
| **Integration - Checkout** | `CheckoutAndPaymentFlowIntegrationTest` | 7 | 0 | 0 | 2.15 s | ✅ PASS |
| **Integration - Cash** | `StockAndCashPaymentIntegrationTest` | 2 | 0 | 0 | 0.98 s | ✅ PASS |
| **Integration - Product** | `ProductCrudIntegrationTest` | 5 | 0 | 0 | 1.45 s | ✅ PASS |
| **Integration - Purchase** | `PurchaseFlowIntegrationTest` | 1 | 0 | 0 | 0.72 s | ✅ PASS |
| **Integration - Purchase** | `PurchaseOrderIntegrationTest` | 2 | 0 | 0 | 0.81 s | ✅ PASS |
| **Load - Concurrency** | `ConcurrentStockReductionLoadTest` | 1 | 0 | 0 | 0.22 s | ✅ PASS |
| **Load - Throughput** | `SystemThroughputLoadTest` | 1 | 0 | 0 | 0.30 s | ✅ PASS |
| **TOTALES** | **19 Clases de Prueba** | **70** | **0** | **0** | **~24 s** | **100% PASS** |

---

## 5. Instrucciones de Ejecución de Pruebas

Para ejecutar la batería de pruebas en cualquier entorno de desarrollo o integración continua (CI/CD), utilizar el wrapper de Maven incluido en el proyecto:

### Ejecución de Toda la Suite (70 tests)
```bash
./mvnw test
```
*En sistemas Windows:*
```cmd
.\mvnw.cmd test
```

### Ejecutar Únicamente las Pruebas Unitarias
```bash
./mvnw test -Dtest="*UnitTest"
```

### Ejecutar Únicamente las Pruebas de Integración
```bash
./mvnw test -Dtest="*IntegrationTest"
```

### Ejecutar Únicamente las Pruebas de Carga y Concurrencia
```bash
./mvnw test -Dtest="*LoadTest"
```

### Ejecutar una Clase o Método Específico
```bash
# Ejecutar una clase específica:
./mvnw test -Dtest=StockServiceUnitTest

# Ejecutar un método específico dentro de una clase:
./mvnw test -Dtest=StockServiceUnitTest#testReduceStockInsufficientException
```
