# SERVICIOS DEL SISTEMA

## Reglas generales para la capa de servicios

- **Lógica de negocio centralizada:** La capa de servicios (`/service`) concentra toda la lógica de negocio, validaciones y reglas operativas del sistema. Los controladores delegan en los servicios y no interactúan directamente con los repositorios.
- **Transaccionalidad:** Los métodos que impliquen modificación de datos o interacción entre múltiples entidades/repositorios deben gestionarse con la anotación `@Transactional` de Spring, garantizando atomicidad (ACID) y consistencia ante fallos.
- **Principios GRASP y SOLID:**
  - *Alta Cohesión y Bajo Acoplamiento:* Cada servicio tiene una única responsabilidad bien delimitada (SRP).
  - *Experto en Información:* Cada servicio opera como la autoridad sobre su agregado o dominio específico (ej. `StockService` es el experto en variaciones de inventario).
  - *Inyección de Dependencias:* Se utiliza inyección por constructor sobre atributos `private final`.
- **Respeto del borrado lógico:** Los servicios deben respetar el atributo `deleted = false` de las entidades, empleando métodos de consulta activa (`findActive`) de los repositorios y ejecutando bajas lógicas (`deleted = true`) salvo que se requiera purga explícita.
- **Uso de DTOs en Reportes y Transferencia:** Se implementa el patrón DTO (*Data Transfer Object*) para encapsular datos calculados, agregados o vistas analíticas (como exige el requerimiento de reportes), evitando exponer entidades persistentes complejas o provocar consultas N+1.

---

## Organización de los servicios por módulo

- **actor:**
  - `UserService`
  - `SupplierService`

- **catalog:**
  - `ProductService`
  - `CategoryService`
  - `PriceService`

- **location:**
  - `LocationService`

- **media:**
  - `ImageService`

- **org:**
  - `StockService`
  - `OfficeService`

- **transaction:**
  - `SaleOrderService`
  - `PurchaseOrderService`
  - `PaymentService`

- **report:**
  - `ReportService`

- **notification:**
  - `EmailService`
  - `NewsletterService`

---

## Detalle de responsabilidades por servicio

### 1. Módulo actor (`ingsoftware.zeroshop.service.actor`)

- **`UserService`**
  - Registro de clientes y administradores con contraseñas encriptadas (`BCryptPasswordEncoder`).
  - Generación, envío y verificación de código de activación de cuenta por correo electrónico.
  - Gestión de datos personales y direcciones del perfil del cliente (`Person`, `Address`, `ContactPhone`).
  - ABM de usuarios por parte del administrador.
  - Implementación del servicio de autenticación de Spring Security (`UserDetailsService`).

- **`SupplierService`**
  - Registro y actualización de proveedores (Razón Social, correo electrónico y teléfono/WhatsApp).
  - Gestión del catálogo de productos provistos y costos de compra asociados (`SupplierProduct`).
  - Consulta de proveedores activos para aprovisionamiento.

---

### 2. Módulo catalog (`ingsoftware.zeroshop.service.catalog`)

- **`ProductService`**
  - ABM de productos (nombre, descripción, talle, stock y estado de oferta).
  - Búsqueda y filtrado por categoría, subcategoría y sección especial de ofertas.
  - Coordinación con almacenamiento de imágenes del producto.

- **`CategoryService`**
  - Gestión de categorías principales (*Niños*, *Niñas*, *Mujeres*, *Hombres*).
  - Gestión de subcategorías (*Ropa*, *Calzado*, *Accesorios*).
  - Consulta de árbol de navegación de categorías y subcategorías para la vista.

- **`PriceService`**
  - Registro y consulta del historial de precios (`PriceHistory`) de cada producto.
  - Determinación del precio vigente de venta.
  - Lógica de ajuste periódico de precios de productos (actualizaciones bimestrales por contexto inflacionario).

---

### 3. Módulo location (`ingsoftware.zeroshop.service.location`)

- **`LocationService`**
  - Consulta jerárquica de ubicaciones geográficas: países, provincias, departamentos y localidades/ciudades.
  - Normalización y persistencia de domicilios (`Address`, `PersonAddress`, `OfficeAddress`).

---

### 4. Módulo media (`ingsoftware.zeroshop.service.media`)

- **`ImageService`**
  - Carga, almacenamiento local o en servidor y vinculación de imágenes multimedia (`Image`).
  - Asociación de recursos visuales con productos (`ProductImage`) y avatares de usuarios (`UserImage`).

---

### 5. Módulo org (`ingsoftware.zeroshop.service.org`)

- **`StockService`**
  - Consulta y balance del inventario general y discriminado por sucursal (`Office`).
  - **Incremento de stock:** Disparado de forma automática al recepcionar una orden de compra de proveedor.
  - **Decremento de stock:** Disparado de forma automática cuando el cliente realiza y abona una orden de venta.
  - Evaluación de criticidad de inventario para semáforo: Bueno (>50%), Regular (20% - 50%) y Malo (<20%).

- **`OfficeService`**
  - ABM de sucursales físicas (`Office`) y asignación de direcciones (`OfficeAddress`).
  - Gestión de empleados y encargados por sucursal (`OfficeEmployee`).

---

### 6. Módulo transaction (`ingsoftware.zeroshop.service.transaction`)

- **`SaleOrderService`**
  - Creación de órdenes de venta a partir del carrito de compras del cliente y sus líneas de detalle (`OrderDetail`).
  - Máquina de estados y seguimiento del pedido: `PENDIENTE DE PAGO` -> `PAGO REALIZADO` -> `PENDIENTE DE ENVIO` -> `ENTREGADO`.
  - Anulación de órdenes por parte del cliente o administrador.
  - Disparo de confirmaciones y envío de comprobante de compra por correo.

- **`PurchaseOrderService`**
  - Generación de órdenes de compra a proveedores especificando producto, cantidad, costo unitario y costo total.
  - Modificación y cancelación de órdenes de compra pendientes.
  - Recepción de mercadería: cambio de estado a entregada y actualización del inventario a través de `StockService`.

- **`PaymentService`**
  - Procesamiento y registro de pagos (`Payment`) con diversos medios (Efectivo, Mercado Pago, etc.).
  - Emisión y persistencia de comprobantes fiscales y facturas (`Invoice`, `InvoiceDetail`).

---

### 7. Módulo report (`ingsoftware.zeroshop.service.report`)

- **`ReportService`**
  - Generación y estructuración de datos analíticos mediante DTOs específicos:
    - **Reporte de Ventas:** Filtro por rango de fechas (ej. mensual), calculando monto total e incluyendo detalle de fecha, producto, categoría, cantidad, ID de compra y medio de pago.
    - **Reporte de Stock por Sucursal:** Cálculo porcentual de existencias respecto al ideal, determinación del estado del semáforo (Bueno, Regular, Malo) y generación de URL directa a la API de WhatsApp con el mensaje preformateado para el proveedor correspondiente.
    - **Reporte de Proveedores Económicos:** Comparación de costos unitarios de compra por producto para determinar la opción más conveniente para reposición.

---

### 8. Módulo notification (`ingsoftware.zeroshop.service.notification`)

- **`EmailService`**
  - Envío de correos transaccionales con soporte de plantillas HTML y texto plano.
  - Envío de código de activación de cuenta en el registro de usuarios.
  - Envío de resumen y confirmación de compra finalizada.

- **`NewsletterService`**
  - Ejecución programada (`@Scheduled`) cada 10 días para armar y despachar correos masivos con productos en oferta a los clientes registrados.

---

## Orquestación y flujos clave de negocio

```
               [Registro de Cliente]
                        │
                  UserService ──(envía código)──> EmailService
                        │
                        ▼
                [Activación Cuenta]

           [Compra / Checkout de Cliente]
                        │
                 SaleOrderService
                  │             │
        (registra)│             │(descuenta stock al pagar)
                  ▼             ▼
            PaymentService  StockService
                  │
                  └──(envía comprobante)──> EmailService

         [Recepción de Mercadería Proveedor]
                        │
              PurchaseOrderService
                        │
                  (aumenta stock)
                        ▼
                   StockService

               [Panel de Reportes]
                        │
                  ReportService
                  ├── SalesReportDTO (Ventas por rango)
                  ├── StockReportDTO (Semáforo + Link WhatsApp Proveedor)
                  └── BestSupplierDTO (Menor costo por producto)
```
