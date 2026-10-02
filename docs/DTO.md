# DTOs del Sistema (Data Transfer Objects) - Zero Shop

Los **Data Transfer Objects (DTOs)** en Zero Shop encapsulan y transfieren datos entre las distintas capas arquitectónicas de la aplicación (`Controller` ↔ `Service` ↔ Vistas Thymeleaf / API REST).

---

## 🏛️ Objetivos y Principios de Diseño

1. **Desacoplamiento del Modelo de Dominio:** Evita exponer directamente entidades JPA (`@Entity`) en formularios web y respuestas de presentación, protegiendo las reglas de persistencia interna y el ciclo de vida de Hibernate.
2. **Seguridad contra Mass Assignment / Over-Posting:** Restringe los datos entrantes a los campos expresamente permitidos, evitando modificaciones accidentales de privilegios, estados de auditoría o IDs ajenos.
3. **Validación Específica del Caso de Uso:** Aplica validaciones de entrada (`jakarta.validation.constraints`) y formatos de conversión (`@DateTimeFormat`) adaptados al formulario.
4. **Optimización de Transferencia y Proyecciones UI:** Permite agregar métricas calculadas (porcentajes de inventario, ahorro entre proveedores, semáforos, enlaces directos a WhatsApp) sin contaminar las tablas de base de datos.
5. **Inmutabilidad en Analítica:** Los DTOs de reportes se implementan como **Java `record`**, garantizando estructuras inmutables, seguras para hilos y de sintaxis concisa.

---

## 📂 Organización de DTOs en el Proyecto

Todos los DTOs globales se ubican bajo el paquete `ingsoftware.zeroshop.dto`:

```
src/main/java/ingsoftware/zeroshop/dto/
├── AddressDTO.java
├── ClientProfileDTO.java
├── DeskProductDTO.java
├── OfficeFormDTO.java
├── PageResult.java
├── SalesReportDTO.java
├── StockReportDTO.java
├── SupplierFormDTO.java
└── SupplierReportDTO.java
```

Adicionalmente, se utilizan *records* locales para proyecciones visuales en controladores específicos:
- `CategoryViewDto` en `ingsoftware.zeroshop.controller.client.CategoryController`
- `CategoryDashboardDto` en `ingsoftware.zeroshop.controller.dashboard.CategoryController`

---

## 📑 Clasificación Funcional

| Tipo de DTO | Clase / Record | Propósito Principal |
| :--- | :--- | :--- |
| **Formularios & Comandos** | `ClientProfileDTO` | Perfil del cliente, credenciales, domicilio y suscripciones |
| | `OfficeFormDTO` | Alta y modificación de sucursales físicas y depósitos |
| | `SupplierFormDTO` | Alta y modificación de proveedores comerciales |
| | `AddressDTO` | Estructura normalizada de direcciones postales |
| **Punto de Venta (POS)** | `DeskProductDTO` | Búsqueda rápida y proyección de productos en mostrador físico |
| **Analítica & Reportes** | `SalesReportDTO` | Consolidación y detalle cronológico de ventas facturadas |
| | `StockReportDTO` | Semáforo de inventario y pedidos de reposición por WhatsApp |
| | `SupplierReportDTO` | Comparador de cotizaciones y cálculo de ahorro por producto |
| **Navegación & Utilidad** | `PageResult<T>` | Contenedor genérico para paginación consistente (1-indexed) |
| **Proyecciones de Vista** | `CategoryViewDto` | Métricas y tarjeta de categoría para el cliente |
| | `CategoryDashboardDto` | Resumen de subcategorías y conteos para administración |

---

## 🔍 Detalle Exhaustivo de cada DTO

---

### 1. `AddressDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Clase (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
* **Propósito:** Estandarizar la carga y transferencia de direcciones postales en la República Argentina. Permite tanto el manejo de texto libre (calle, número, piso, depto, observaciones) como el enlace relacional normalizado con las divisiones políticas (`Country`, `State`, `City`).
* **Uso:** Es utilizado como componente anidado dentro de `OfficeFormDTO` y `SupplierFormDTO`, permitiendo binding bidireccional en formularios Thymeleaf (`th:field="*{address.street}"`).

#### Campos
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `street` | `String` | Nombre de la arteria o calle principal |
| `number` | `String` | Numeración de la dirección |
| `zipCode` | `String` | Código Postal (ej: `5500`, `M5500`) |
| `floor` | `String` | Número de piso (opcional para departamentos) |
| `apartment` | `String` | Letra o número de departamento (opcional) |
| `observations` | `String` | Indicaciones adicionales para entrega o despacho |
| `countryId` | `UUID` | Identificador único del país asociado |
| `stateId` | `UUID` | Identificador único de la provincia asociada |
| `cityId` | `UUID` | Identificador único de la ciudad / municipio |

---

### 2. `ClientProfileDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Clase (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
* **Propósito:** Modela el formulario integral de administración de perfil del cliente (`/profile`). Agrupa datos personales, domicilio preferido de entrega en la provincia de Mendoza, credenciales de cuenta y opciones de marketing.
* **Uso:** Transferencia entre `ProfileController` y `UserService.updateUserProfile(...)`.

#### Campos
| Sección | Campo | Tipo | Descripción / Restricciones |
| :--- | :--- | :--- | :--- |
| **Datos Personales** | `firstName` | `String` | Nombre de pila del cliente |
| | `lastName` | `String` | Apellido del cliente |
| | `gender` | `Gender` | Género del cliente (`MALE`, `FEMALE`, `OTHER`) |
| | `dateOfBirth` | `LocalDate` | Fecha de nacimiento (`@DateTimeFormat(iso = ISO.DATE)`) |
| | `phone` | `String` | Teléfono de contacto / WhatsApp |
| **Domicilio (Mendoza)**| `state` | `String` | Provincia (por defecto `Mendoza`) |
| | `department` | `String` | Departamento / Jurisdicción (ej: `Capital`, `Godoy Cruz`) |
| | `city` | `String` | Ciudad o localidad |
| | `street` | `String` | Calle del domicilio |
| | `number` | `String` | Altura catastral / número |
| | `zipCode` | `String` | Código postal |
| | `floor` | `String` | Piso |
| | `apartment` | `String` | Departamento |
| **Cuenta & Seguridad** | `email` | `String` | Correo electrónico principal de la cuenta |
| | `password` | `String` | Nueva contraseña (opcional; si se omite, se conserva la actual) |
| **Preferencias** | `emailPromotionsEnabled` | `Boolean` | Flag para suscripción al boletín de ofertas y promociones |

---

### 3. `DeskProductDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Clase (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
* **Propósito:** Optimizar el rendimiento y la experiencia del Punto de Venta (POS) / Venta Directa en mostrador para los empleados (`/dashboard/pos`). Proyecta únicamente los datos mínimos indispensables que necesita la terminal de caja sin sobrecargar la red.
* **Uso:** Utilizado en `EmployeeController` para la vista inicial del mostrador y el endpoint de autocompletado en tiempo real `/dashboard/pos/search-products`.

#### Campos
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `UUID` | Identificador único del producto |
| `code` | `String` | Código de barras o SKU del producto |
| `name` | `String` | Nombre comercial del artículo |
| `category` | `String` | Nombre de la categoría a la que pertenece |
| `price` | `BigDecimal` | Precio vigente al público |
| `stock` | `Integer` | Unidades físicas disponibles en la sucursal del empleado |
| `imageUrl` | `String` | URL de la imagen del producto |
| `onSale` | `Boolean` | Indica si el producto cuenta con descuento u oferta activa |

---

### 4. `OfficeFormDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Clase (`@Data`)
* **Propósito:** Captura y valida los datos para la creación y edición de sucursales físicas, depósitos y casas centrales en el panel de administración (`/dashboard/admin/offices`).
* **Uso:** `OfficeController` y `OfficeService` (`createOffice`, `updateOffice`).

#### Campos
| Campo | Tipo | Validación | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | Opcional | Identificador de la sucursal (presente en edición) |
| `name` | `String` | `@NotBlank` | Nombre descriptivo de la sucursal |
| `cuit` | `String` | `@NotBlank` | Clave Única de Identificación Tributaria |
| `phone` | `String` | Opcional | Teléfono fijo o móvil de la sucursal |
| `type` | `String` | `@NotBlank` | Tipo (`HEADQUARTERS`, `BRANCH`, `WAREHOUSE`) |
| `address` | `AddressDTO` | Instancia por defecto | Objeto anidado con la dirección física completa |

---

### 5. `SupplierFormDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Clase (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
* **Propósito:** Modelo de formulario para la gestión de proveedores mayoristas de indumentaria y calzado deportivo (`/dashboard/providers`).
* **Uso:** `ProviderController` y `SupplierService` (`createSupplier`, `updateSupplier`).

#### Campos
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `UUID` | Identificador único del proveedor |
| `name` | `String` | Razón social o nombre comercial |
| `cuit` | `String` | CUIT fiscal del proveedor |
| `email` | `String` | Correo corporativo para órdenes de compra |
| `phone` | `String` | Teléfono de contacto comercial |
| `address` | `AddressDTO` | Domicilio legal o depósito central del proveedor |

---

### 6. `SalesReportDTO` & `SaleItemDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Java `record` inmutable
* **Propósito:** Consolidar métricas y líneas de detalle de ventas facturadas en un rango temporal determinado para la toma de decisiones directivas (`/dashboard/admin/reports/sales`).
* **Uso:** Producido por `ReportService.getSalesReport(startDate, endDate)` y consumido por `ReportController`.

#### Estructura de `SalesReportDTO`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `startDate` | `LocalDate` | Fecha inicial del rango consultado |
| `endDate` | `LocalDate` | Fecha final del rango consultado |
| `totalBilled` | `BigDecimal` | Sumatoria total neta facturada en pesos |
| `totalOrders` | `int` | Cantidad total de órdenes cobradas en el período |
| `details` | `List<SaleItemDTO>` | Listado cronológico de ítems vendidos |

#### Sub-record: `SaleItemDTO`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `orderId` | `String` | Identificador abreviado del pedido |
| `date` | `LocalDateTime` | Fecha y hora exacta en la que se confirmó el pago |
| `productName` | `String` | Nombre del producto comercializado |
| `categoryName` | `String` | Categoría del producto |
| `quantity` | `Integer` | Unidades vendidas en esa línea |
| `paymentMethod` | `String` | Método de pago (`MERCADO_PAGO`, `CASH`, `CREDIT`, `DEBIT`) |
| `amount` | `BigDecimal` | Subtotal facturado correspondiente a la línea |

---

### 7. `StockReportDTO` & `StockItemDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Java `record` inmutable
* **Propósito:** Brindar una visión integral del estado del inventario en todas las sucursales, aplicando un semáforo de abastecimiento visual y facilitando la reposición inmediata vía WhatsApp con el proveedor (`/dashboard/admin/reports/stock`).
* **Uso:** Producido por `ReportService.getStockReport()` y consumido por `ReportController`.

#### Estructura de `StockReportDTO`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `totalStockUnits` | `int` | Cantidad total acumulada de unidades físicas |
| `pctGood` | `double` | Porcentaje de ítems en estado óptimo (verde: ≥ 70%) |
| `pctRegular` | `double` | Porcentaje de ítems en estado advertencia (amarillo: 30% a 69%) |
| `pctBad` | `double` | Porcentaje de ítems en estado crítico / rotura (rojo: < 30%) |
| `details` | `List<StockItemDTO>` | Auditoría detallada por artículo y sucursal |

#### Sub-record: `StockItemDTO`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `officeName` | `String` | Nombre de la sucursal donde reside el stock |
| `productName` | `String` | Nombre del producto |
| `productCode` | `String` | Código SKU o de barras |
| `productSize` | `String` | Talle del artículo (ej: `S`, `M`, `L`, `XL`, `NUM_40`, etc.) |
| `quantity` | `int` | Stock físico actual |
| `capacity` | `int` | Capacidad o stock objetivo asignado |
| `percentage` | `int` | Nivel porcentual respecto a la capacidad (0 a 100%) |
| `trafficLight` | `String` | Categorización del semáforo: `"BIEN"`, `"REGULAR"` o `"MALO"` |
| `neededUnits` | `int` | Cantidad de unidades requeridas para volver al stock ideal |
| `supplierName` | `String` | Proveedor habitual para reabastecimiento |
| `supplierPhone` | `String` | Teléfono del proveedor |
| `whatsappUrl` | `String` | Enlace `https://wa.me/...` con mensaje preformateado para pedir reposición en un clic |

---

### 8. `SupplierReportDTO` & `SupplierComparisonDTO`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Java `record` inmutable
* **Propósito:** Comparar cotizaciones de costo entre múltiples proveedores para el mismo producto, identificando la mejor opción de compra y calculando el margen de ahorro unitario y porcentual (`/dashboard/admin/reports/suppliers`).
* **Uso:** Producido por `ReportService.getSuppliersReport()` y consumido por `ReportController`.

#### Estructura de `SupplierReportDTO`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `comparisons` | `List<SupplierComparisonDTO>` | Lista comparativa de artículos con cotizaciones activas |

#### Sub-record: `SupplierComparisonDTO`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `productId` | `UUID` | Identificador del producto |
| `productName` | `String` | Nombre del artículo analizado |
| `productCode` | `String` | Código SKU |
| `categoryName` | `String` | Categoría a la que pertenece |
| `bestSupplierName` | `String` | Nombre del proveedor más conveniente (menor costo) |
| `bestCostPrice` | `BigDecimal` | Precio de compra más bajo obtenido |
| `altSupplierName` | `String` | Proveedor alternativo con precio más alto |
| `altCostPrice` | `BigDecimal` | Precio de compra del proveedor alternativo |
| `unitSavings` | `BigDecimal` | Diferencia absoluta de ahorro por unidad (`altCostPrice - bestCostPrice`) |
| `savingPercentage` | `Double` | Porcentaje de ahorro económico relativo |

---

### 9. `PageResult<T>`
* **Paquete:** `ingsoftware.zeroshop.dto`
* **Tipo:** Clase genérica (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
* **Propósito:** Proporciona paginación universal en memoria desacoplada de la capa de persistencia. Garantiza que cualquier lista de entidades o DTOs pueda paginarse con navegación 1-indexed (fácilmente comprensible en URLs como `?page=1`).
* **Uso:** Utilizado en prácticamente todos los listados de la plataforma:
  - Catálogo de productos del cliente (`/products`, `/offers`)
  - Listado de compras del cliente (`/orders`)
  - Tablas del dashboard: productos, stock, compras a proveedores, ventas, sucursales y usuarios.

#### Método de Fábrica Estático
```java
public static <T> PageResult<T> of(List<T> allItems, int page, int size)
```
Calcula de manera segura y automática el subconjunto de elementos, protegiendo contra páginas vacías, páginas fuera de rango y tamaños menores a 1.

#### Campos
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `content` | `List<T>` | Elementos correspondientes a la página solicitada |
| `pageNumber` | `int` | Número de página actual (base 1) |
| `pageSize` | `int` | Cantidad de registros por página |
| `totalElements` | `long` | Cantidad total absoluta de elementos en la colección |
| `totalPages` | `int` | Total de páginas resultantes (mínimo 1) |
| `hasPrevious` | `boolean` | `true` si existe una página anterior |
| `hasNext` | `boolean` | `true` si existe una página posterior |
| `first` | `boolean` | `true` si se encuentra en la primera página |
| `last` | `boolean` | `true` si se encuentra en la última página |
| `fromIndex` | `int` | Índice ordinal del primer elemento mostrado (ej: "Mostrando 1 a 10") |
| `toIndex` | `int` | Índice ordinal del último elemento mostrado |

---

### 10. Proyecciones Auxiliares de Vistas (Records Locales en Controladores)

#### `CategoryViewDto`
* **Ubicación:** `ingsoftware.zeroshop.controller.client.CategoryController`
* **Tipo:** Record local
* **Propósito:** Proyectar la tarjeta visual de categoría en el portal público de clientes (`/categories`), incluyendo estadísticas en vivo de productos y ofertas.
* **Campos:** `id` (`UUID`), `name` (`String`), `subcategoriesCount` (`int`), `productsCount` (`int`), `onSaleProductsCount` (`int`), `imageUrl` (`String`).

#### `CategoryDashboardDto`
* **Ubicación:** `ingsoftware.zeroshop.controller.dashboard.CategoryController`
* **Tipo:** Record local
* **Propósito:** Proyectar la fila resumida de cada categoría en la grilla administrativa (`/dashboard/categories`).
* **Campos:** `id` (`UUID`), `name` (`String`), `subcategories` (`String` concatenado por comas), `productsCount` (`int`).

---

## 🛡️ Reglas y Convenciones del Proyecto para Nuevos DTOs

Al incorporar nuevos DTOs a la arquitectura de Zero Shop:
1. **Ubicación Obligatoria:** Colocarlos en el paquete `ingsoftware.zeroshop.dto` (a menos que sean records estrictamente privados de una vista de controlador).
2. **Convención de Nombres:**
   - Sufijo `DTO` para objetos de transferencia general y proyecciones (ej: `ProductSummaryDTO`).
   - Sufijo `FormDTO` para objetos destinados a captura de formularios con validación (ej: `PromotionFormDTO`).
3. **Inmutabilidad Preferida:** Para reportes y respuestas solo-lectura, implementar siempre **Java `record`**.
4. **Respeto de Capas:** Los controladores deben recibir o construir DTOs y enviarlos a la capa `Service`. Los repositorios nunca deben depender de DTOs de presentación web.